package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.dto.response.AgentResponse;
import com.hyan.zealinklybackend.dto.response.TaskResponse;
import com.hyan.zealinklybackend.dto.response.TodoItem;
import com.hyan.zealinklybackend.entity.Elder;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.ElderRepository;
import com.hyan.zealinklybackend.service.intent.IntentRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent服务：统一处理用户输入，自动路由到对应功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final IntentRecognitionService intentRecognitionService;
    private final TaskService taskService;
    private final ElderRepository elderRepository;

    /**
     * 处理用户输入，自动识别意图并执行相应操作
     */
    @Transactional
    public AgentResponse processUserInput(Long elderId, String userInput) {
        log.info("Processing user input for elder {}: {}", elderId, userInput);
        
        // 1. 意图识别和任务提取
        List<TodoItem> tasks = intentRecognitionService.extractTasks(userInput);
        
        String intentType;
        String intentDescription;
        List<TaskResponse> createdTasks = new ArrayList<>();
        String aiResponse = null;
        String message;
        
        // 2. 确定基础意图
        if (tasks != null && !tasks.isEmpty()) {
            boolean hasEmergency = tasks.stream()
                    .anyMatch(task -> "EMERGENCY".equals(task.getType()));
            if (hasEmergency) {
                intentType = "EMERGENCY";
                intentDescription = "紧急报警";
            } else {
                intentType = "MUTUAL_AID";
                intentDescription = "互助任务";
            }
        } else {
            // 没有提取到任务，使用简单意图识别
            String simpleIntent;
            try {
                String rawIntent = intentRecognitionService.recognizeIntent(userInput);
                if (rawIntent == null || rawIntent.trim().isEmpty()) {
                    simpleIntent = "AI_CHAT"; // 默认
                } else {
                    simpleIntent = rawIntent.trim().toUpperCase();
                }
            } catch (Exception e) {
                log.warn("Simple intent recognition failed, defaulting to AI_CHAT", e);
                simpleIntent = "AI_CHAT"; // 默认
            }
            
            // 规范化意图类型
            if (!simpleIntent.equals("MUTUAL_AID") && 
                !simpleIntent.equals("EMERGENCY") && 
                !simpleIntent.equals("AI_CHAT")) {
                if (simpleIntent.contains("MUTUAL") || simpleIntent.contains("互助")) {
                    simpleIntent = "MUTUAL_AID";
                } else if (simpleIntent.contains("EMERGENCY") || simpleIntent.contains("紧急")) {
                    simpleIntent = "EMERGENCY";
                } else {
                    simpleIntent = "AI_CHAT"; // 默认
                }
            }
            
            intentType = simpleIntent;
            intentDescription = switch (intentType) {
                case "MUTUAL_AID" -> "互助任务";
                case "EMERGENCY" -> "紧急报警";
                case "AI_CHAT" -> "AI聊天";
                default -> "未知";
            };
        }
        
        // 3. 根据意图执行相应操作
        switch (intentType) {
            case "EMERGENCY":
                // 处理紧急报警
                List<TodoItem> emergencyTasks = (tasks != null ? tasks : new ArrayList<TodoItem>()).stream()
                        .filter(task -> "EMERGENCY".equals(task.getType()))
                        .collect(Collectors.toList());
                
                if (emergencyTasks.isEmpty()) {
                    // 如果没有提取到紧急任务，但意图是紧急，创建一个紧急任务
                    // 使用用户输入作为内容，只在有明确位置时才添加位置
                    String location = getUserLocation(elderId);
                    String content = userInput;
                    if (!"当前位置".equals(location)) {
                        content = location + " - " + content;
                    }
                    TaskResponse emergencyTask = taskService.triggerEmergency(
                            elderId, 
                            content
                    );
                    createdTasks.add(emergencyTask);
                    message = "紧急报警已发出，救援正在路上";
                } else {
                    // 为每个紧急任务创建报警
                    for (TodoItem task : emergencyTasks) {
                        // 只在用户有明确位置信息时才添加位置，否则只使用任务描述
                        String location = getUserLocation(elderId);
                        String content = task.getDescription();
                        // 如果用户有地址或坐标，才添加位置信息
                        if (!"当前位置".equals(location)) {
                            content = location + " - " + content;
                        }
                        TaskResponse emergencyTask = taskService.triggerEmergency(
                                elderId,
                                content
                        );
                        createdTasks.add(emergencyTask);
                    }
                    message = String.format("已发出 %d 个紧急报警，救援正在路上", emergencyTasks.size());
                }
                break;
                
            case "MUTUAL_AID":
                // 处理互助任务
                List<TodoItem> mutualAidTasks = (tasks != null ? tasks : new ArrayList<TodoItem>()).stream()
                        .filter(task -> "MUTUAL_AID".equals(task.getType()))
                        .collect(Collectors.toList());
                
                if (mutualAidTasks.isEmpty()) {
                    // 如果没有提取到任务，但意图是互助，创建一个任务
                    // 从用户输入中提取标题
                    String title = extractTitle(userInput);
                    // 如果title和userInput相同，只传title避免重复
                    String description = title.equals(userInput) ? null : userInput;
                    TaskResponse task = taskService.publishCooperation(
                            elderId,
                            title,
                            description,
                            0
                    );
                    createdTasks.add(task);
                    message = "互助任务已发布";
                } else {
                    // 为每个互助任务创建任务
                    for (TodoItem task : mutualAidTasks) {
                        String description = task.getDescription();
                        // 如果描述较短（<=20字符），title和description相同，只传description避免重复
                        String title = extractTitle(description);
                        // 如果title和description相同，只传description
                        String finalDescription = title.equals(description) ? description : description;
                        TaskResponse publishedTask = taskService.publishCooperation(
                                elderId,
                                title,
                                finalDescription,
                                0 // 默认不设置积分奖励
                        );
                        createdTasks.add(publishedTask);
                    }
                    message = String.format("已发布 %d 个互助任务", mutualAidTasks.size());
                }
                break;
                
            case "AI_CHAT":
            default:
                // AI聊天
                aiResponse = taskService.askAi(elderId, userInput);
                message = "AI回复已生成";
                break;
        }
        
        return AgentResponse.builder()
                .intentType(intentType)
                .intentDescription(intentDescription)
                .userInput(userInput)
                .tasks(tasks != null ? tasks : new ArrayList<>())
                .message(message)
                .createdTasks(createdTasks.isEmpty() ? null : createdTasks)
                .aiResponse(aiResponse)
                .build();
    }
    
    /**
     * 获取用户位置信息
     */
    private String getUserLocation(Long elderId) {
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new BusinessException("老人不存在"));
        
        if (elder.getAddress() != null && !elder.getAddress().isEmpty()) {
            return elder.getAddress();
        }
        
        if (elder.getLat() != null && elder.getLng() != null) {
            return String.format("位置：%s, %s", elder.getLat(), elder.getLng());
        }
        
        return "当前位置";
    }
    
    /**
     * 从任务描述中提取标题（取前20个字符）
     */
    private String extractTitle(String description) {
        if (description == null || description.isEmpty()) {
            return "互助任务";
        }
        
        // 如果描述超过20个字符，截取前20个字符
        if (description.length() > 20) {
            return description.substring(0, 20) + "...";
        }
        
        return description;
    }
}
