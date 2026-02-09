package com.hyan.zealinklybackend.dto.response;

import com.hyan.zealinklybackend.entity.Task;
import com.hyan.zealinklybackend.entity.TaskStatus;
import com.hyan.zealinklybackend.entity.TaskType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 任务统一响应 DTO（避免暴露实体与懒加载）
 */
@Data
@Builder
public class TaskResponse {
    private Long id;
    private TaskType taskType;
    private TaskStatus status;
    private Long elderId;
    private String elderName;
    private String elderPhone;
    private Long volunteerId;
    private String volunteerName;
    private String volunteerPhone;
    private Long adminId;
    private String adminName;
    private String content;
    private String aiResponse;
    private Integer pointsReward;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    /** 任务凭证（仅详情或 SUBMITTED/COMPLETED 时可能有） */
    @Builder.Default
    private List<EvidenceItemResponse> evidenceList = null;

    public static TaskResponse fromEntity(Task task) {
        return fromEntity(task, null);
    }

    public static TaskResponse fromEntity(Task task, List<EvidenceItemResponse> evidenceList) {
        if (task == null) return null;
        return TaskResponse.builder()
                .id(task.getId())
                .taskType(task.getTaskType())
                .status(task.getStatus())
                .elderId(task.getElder() != null ? task.getElder().getId() : null)
                .elderName(task.getElder() != null ? task.getElder().getRealName() : null)
                .elderPhone(task.getElder() != null ? task.getElder().getPhone() : null)
                .volunteerId(task.getVolunteer() != null ? task.getVolunteer().getId() : null)
                .volunteerName(task.getVolunteer() != null ? task.getVolunteer().getRealName() : null)
                .volunteerPhone(task.getVolunteer() != null ? task.getVolunteer().getPhone() : null)
                .adminId(task.getAdmin() != null ? task.getAdmin().getId() : null)
                .adminName(task.getAdmin() != null ? task.getAdmin().getRealName() : null)
                .content(task.getContent())
                .aiResponse(task.getAiResponse())
                .pointsReward(task.getPointsReward() != null ? task.getPointsReward() : 0)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .evidenceList(evidenceList)
                .build();
    }
}
