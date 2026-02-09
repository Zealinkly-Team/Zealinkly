package com.hyan.zealinklybackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyan.zealinklybackend.dto.request.EvidenceItemRequest;
import com.hyan.zealinklybackend.dto.response.AiChatItemResponse;
import com.hyan.zealinklybackend.dto.response.EvidenceItemResponse;
import com.hyan.zealinklybackend.dto.response.TaskResponse;
import com.hyan.zealinklybackend.entity.*;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 统一任务服务：互助任务、紧急报警、AI 聊天
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";
    private static final String AI_SYSTEM_PROMPT = "你是一个社区助老志愿者，请用温和、简洁、易懂的语言回答老人的问题。";

    private final TaskRepository taskRepository;
    private final ElderRepository elderRepository;
    private final VolunteerRepository volunteerRepository;
    private final AdminRepository adminRepository;
    private final PointsLedgerRepository pointsLedgerRepository;
    private final TaskEvidenceRepository taskEvidenceRepository;
    private final AppealRepository appealRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${app.points.default-task-reward:0}")
    private int defaultTaskReward;

    // ===================== 互助任务 =====================

    @Transactional
    public TaskResponse publishCooperation(Long elderId, String title, String description, Integer pointsReward) {
        Elder elder = elderRepository.findById(elderId).orElseThrow(() -> new BusinessException("老人不存在"));
        String content = buildCooperationContent(title, description);
        int reward = pointsReward != null && pointsReward > 0 ? pointsReward : defaultTaskReward;

        Task task = Task.builder()
                .taskType(TaskType.COOPERATION)
                .status(TaskStatus.PENDING)
                .elder(elder)
                .content(content)
                .pointsReward(reward)
                .build();
        task = taskRepository.save(task);
        taskRepository.flush();
        task.getElder().getId();
        task.getElder().getRealName();
        task.getElder().getPhone();
        return TaskResponse.fromEntity(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAvailableCooperationTasks() {
        List<Task> list = taskRepository.findByTaskTypeAndStatusOrderByCreatedAtDesc(TaskType.COOPERATION, TaskStatus.PENDING);
        return list.stream().map(TaskResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse acceptCooperation(Long taskId, Long volunteerId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new BusinessException("任务不存在"));
        if (task.getTaskType() != TaskType.COOPERATION) {
            throw new BusinessException("任务类型错误");
        }
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new BusinessException("该任务已被接单或已结束");
        }
        Volunteer volunteer = volunteerRepository.findById(volunteerId).orElseThrow(() -> new BusinessException("志愿者不存在"));

        task.setVolunteer(volunteer);
        task.setStatus(TaskStatus.CLAIMED);
        task = taskRepository.save(task);
        task = taskRepository.findByIdWithAssociations(task.getId()).orElse(task);
        return TaskResponse.fromEntity(task);
    }

    /** 志愿者：开始服务（可选，便于老人端看到“进行中”） */
    @Transactional
    public TaskResponse startCooperation(Long taskId, Long volunteerId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new BusinessException("任务不存在"));
        if (task.getTaskType() != TaskType.COOPERATION) {
            throw new BusinessException("任务类型错误");
        }
        if (task.getVolunteer() == null || !task.getVolunteer().getId().equals(volunteerId)) {
            throw new BusinessException("只能操作自己接下的任务");
        }
        if (task.getStatus() != TaskStatus.CLAIMED) {
            throw new BusinessException("当前状态不允许开始服务");
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
        task = taskRepository.save(task);
        task = taskRepository.findByIdWithAssociations(task.getId()).orElse(task);
        return TaskResponse.fromEntity(task);
    }

    /** 志愿者：提交完成 + 上传凭证，等待老人确认交接 */
    @Transactional
    public TaskResponse volunteerSubmitCompletion(Long taskId, Long volunteerId, String note, List<EvidenceItemRequest> evidences) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new BusinessException("任务不存在"));
        if (task.getTaskType() != TaskType.COOPERATION) {
            throw new BusinessException("任务类型错误");
        }
        if (task.getVolunteer() == null || !task.getVolunteer().getId().equals(volunteerId)) {
            throw new BusinessException("只能提交自己接下的任务");
        }
        if (task.getStatus() != TaskStatus.CLAIMED && task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new BusinessException("当前状态不允许提交完成");
        }

        task.setStatus(TaskStatus.SUBMITTED);
        task.setAiResponse(note);
        task = taskRepository.save(task);

        if (evidences != null && !evidences.isEmpty()) {
            for (EvidenceItemRequest req : evidences) {
                if (req == null || req.getEvidenceType() == null) continue;
                TaskEvidence ev = TaskEvidence.builder()
                        .task(task)
                        .evidenceType(req.getEvidenceType())
                        .fileUrl(req.getFileUrl())
                        .build();
                taskEvidenceRepository.save(ev);
            }
        }
        task = taskRepository.findByIdWithAssociations(task.getId()).orElse(task);
        return TaskResponse.fromEntity(task);
    }

    /** 老人：确认交接，任务完成，写入积分流水并更新双方余额 */
    @Transactional
    public TaskResponse elderConfirmCompletion(Long taskId, Long elderId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new BusinessException("任务不存在"));
        if (task.getTaskType() != TaskType.COOPERATION) {
            throw new BusinessException("任务类型错误");
        }
        if (task.getElder() == null || !task.getElder().getId().equals(elderId)) {
            throw new BusinessException("只能确认自己发布的任务");
        }
        if (task.getStatus() != TaskStatus.SUBMITTED) {
            throw new BusinessException("请等待志愿者提交完成后再确认交接");
        }

        Elder elder = task.getElder();
        Volunteer volunteer = task.getVolunteer();
        int reward = task.getPointsReward() != null ? task.getPointsReward() : 0;

        int elderBalance = elder.getPoints() != null ? elder.getPoints() : 0;
        if (reward > 0 && elderBalance < reward) {
            throw new BusinessException("您的积分余额不足，无法完成交接（需要 " + reward + " 积分）");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task = taskRepository.save(task);

        if (reward > 0 && volunteer != null) {
            int newElderBalance = elderBalance - reward;
            int volunteerBalance = volunteer.getPoints() != null ? volunteer.getPoints() : 0;
            int newVolunteerBalance = volunteerBalance + reward;

            elder.setPoints(newElderBalance);
            volunteer.setPoints(newVolunteerBalance);
            elderRepository.save(elder);
            volunteerRepository.save(volunteer);

            PointsLedger elderLedger = PointsLedger.builder()
                    .userType("ELDER")
                    .userId(elder.getId())
                    .amount(-reward)
                    .balanceAfter(newElderBalance)
                    .reason("TASK_COST")
                    .task(task)
                    .build();
            PointsLedger volunteerLedger = PointsLedger.builder()
                    .userType("VOLUNTEER")
                    .userId(volunteer.getId())
                    .amount(reward)
                    .balanceAfter(newVolunteerBalance)
                    .reason("TASK_REWARD")
                    .task(task)
                    .build();
            pointsLedgerRepository.save(elderLedger);
            pointsLedgerRepository.save(volunteerLedger);
        }

        task = taskRepository.findByIdWithAssociations(task.getId()).orElse(task);
        return TaskResponse.fromEntity(task);
    }

    /** 获取互助任务详情（含凭证列表），仅该任务的老人或志愿者可查看 */
    @Transactional(readOnly = true)
    public TaskResponse getCooperationTaskDetail(Long taskId, Long userId, String userType) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new BusinessException("任务不存在"));
        if (task.getTaskType() != TaskType.COOPERATION) {
            throw new BusinessException("任务类型错误");
        }
        boolean isElder = task.getElder() != null && task.getElder().getId().equals(userId);
        boolean isVolunteer = task.getVolunteer() != null && task.getVolunteer().getId().equals(userId);
        if (!isElder && !isVolunteer) {
            throw new BusinessException("仅任务的发布者或接单志愿者可查看详情");
        }
        List<TaskEvidence> evidenceList = taskEvidenceRepository.findByTaskIdOrderByCreatedAtAsc(task.getId());
        List<EvidenceItemResponse> evidenceResponses = evidenceList.stream()
                .map(EvidenceItemResponse::fromEntity)
                .collect(Collectors.toList());
        return TaskResponse.fromEntity(task, evidenceResponses);
    }

    /** 老人或志愿者：针对互助任务提交申诉 */
    @Transactional
    public void submitAppeal(Long taskId, Long userId, String userType, String content) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new BusinessException("任务不存在"));
        if (task.getTaskType() != TaskType.COOPERATION) {
            throw new BusinessException("仅支持对互助任务提交申诉");
        }
        if (!"ELDER".equals(userType) && !"VOLUNTEER".equals(userType)) {
            throw new BusinessException("仅老人或志愿者可提交申诉");
        }
        boolean isElder = task.getElder() != null && task.getElder().getId().equals(userId);
        boolean isVolunteer = task.getVolunteer() != null && task.getVolunteer().getId().equals(userId);
        if (!isElder && !isVolunteer) {
            throw new BusinessException("仅该任务的发布者或接单志愿者可提交申诉");
        }
        Appeal appeal = Appeal.builder()
                .task(task)
                .complainantType(userType)
                .complainantId(userId)
                .content(content)
                .status("PENDING")
                .build();
        appealRepository.save(appeal);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyCooperationAsElder(Long elderId) {
        List<Task> list = taskRepository.findByTaskTypeAndElderIdOrderByCreatedAtDesc(TaskType.COOPERATION, elderId);
        return list.stream().map(TaskResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyCooperationAsVolunteer(Long volunteerId) {
        List<Task> list = taskRepository.findByTaskTypeAndVolunteerIdOrderByCreatedAtDesc(TaskType.COOPERATION, volunteerId);
        return list.stream().map(TaskResponse::fromEntity).collect(Collectors.toList());
    }

    // ===================== 紧急报警 =====================

    @Transactional
    public TaskResponse triggerEmergency(Long elderId, String location) {
        Elder elder = elderRepository.findById(elderId).orElseThrow(() -> new BusinessException("老人不存在"));

        Task task = Task.builder()
                .taskType(TaskType.EMERGENCY)
                .status(TaskStatus.PENDING)
                .elder(elder)
                .content(location)
                .build();
        task = taskRepository.save(task);

        String message = String.format("紧急报警：%s 在 %s 发起求助！任务ID：%d",
                elder.getRealName() != null ? elder.getRealName() : elder.getUsername(),
                location, task.getId());
        messagingTemplate.convertAndSend("/topic/emergency", message);

        return TaskResponse.fromEntity(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getPendingEmergencies() {
        List<Task> list = taskRepository.findByTaskTypeAndStatusOrderByCreatedAtDesc(TaskType.EMERGENCY, TaskStatus.PENDING);
        return list.stream().map(TaskResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse handleEmergency(Long taskId, Long adminId, String note) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new BusinessException("报警记录不存在"));
        if (task.getTaskType() != TaskType.EMERGENCY) {
            throw new BusinessException("任务类型错误");
        }
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new BusinessException("该报警已处理");
        }
        Admin admin = adminRepository.findById(adminId).orElseThrow(() -> new BusinessException("管理员不存在"));

        task.setAdmin(admin);
        task.setStatus(TaskStatus.COMPLETED);
        task.setAiResponse(note);
        task = taskRepository.save(task);
        task = taskRepository.findByIdWithAssociations(task.getId()).orElse(task);
        return TaskResponse.fromEntity(task);
    }

    // ===================== AI 聊天 =====================

    @Transactional
    public String askAi(Long elderId, String question) {
        Elder elder = elderRepository.findById(elderId).orElseThrow(() -> new BusinessException("老人不存在"));

        String answer;
        if (deepseekApiKey != null && !deepseekApiKey.isBlank()) {
            log.info("调用 DeepSeek API，问题: {}", question);
            try {
                answer = callDeepSeek(question);
                log.info("DeepSeek API 调用成功");
            } catch (Exception e) {
                log.error("DeepSeek API 调用失败: {}", e.getMessage(), e);
                answer = "AI 服务暂时不可用，请稍后再试。错误: " + e.getMessage();
            }
        } else {
            log.warn("DeepSeek API Key 未配置");
            answer = "AI 服务暂未配置，请联系管理员配置 DeepSeek API Key。";
        }

        Task task = Task.builder()
                .taskType(TaskType.AI_CHAT)
                .status(TaskStatus.COMPLETED)
                .elder(elder)
                .content(question)
                .aiResponse(answer)
                .build();
        taskRepository.save(task);

        return answer;
    }

    public List<AiChatItemResponse> getAiChatHistory(Long elderId) {
        List<Task> list = taskRepository.findByTaskTypeAndElderIdOrderByCreatedAtDesc(TaskType.AI_CHAT, elderId);
        return list.stream()
                .map(t -> AiChatItemResponse.builder()
                        .taskId(t.getId())
                        .question(t.getContent())
                        .answer(t.getAiResponse())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ===================== 私有方法 =====================

    private static String buildCooperationContent(String title, String description) {
        if (description == null || description.isBlank()) {
            return title != null ? title : "";
        }
        return (title != null ? title : "") + "\n\n" + description;
    }

    private String callDeepSeek(String question) {
        OkHttpClient client = new OkHttpClient();
        String systemEscaped = escapeJson(AI_SYSTEM_PROMPT);
        String userEscaped = escapeJson(question);
        String jsonBody = """
                {
                  "model": "deepseek-chat",
                  "messages": [
                    {"role": "system", "content": "%s"},
                    {"role": "user", "content": "%s"}
                  ]
                }
                """.formatted(systemEscaped, userEscaped);

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(DEEPSEEK_API_URL)
                .addHeader("Authorization", "Bearer " + deepseekApiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            log.debug("DeepSeek API 响应状态: {}, 响应体: {}", response.code(), responseBody);
            
            if (!response.isSuccessful()) {
                log.error("DeepSeek API 调用失败，状态码: {}, 响应: {}", response.code(), responseBody);
                throw new BusinessException("AI 服务调用失败，状态码: " + response.code() + "，响应: " + responseBody);
            }
            
            JsonNode node = objectMapper.readTree(responseBody);
            if (!node.has("choices") || node.path("choices").size() == 0) {
                log.error("DeepSeek API 响应格式异常: {}", responseBody);
                throw new BusinessException("AI 服务返回格式异常");
            }
            
            String content = node.path("choices").get(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                log.error("DeepSeek API 返回内容为空: {}", responseBody);
                throw new BusinessException("AI 服务返回内容为空");
            }
            
            return content;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("DeepSeek API 调用异常: {}", e.getMessage(), e);
            throw new BusinessException("AI 服务调用异常: " + e.getMessage());
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
