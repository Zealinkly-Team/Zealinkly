package com.hyan.zealinklybackend.dto.request;

import com.hyan.zealinklybackend.entity.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminTaskUpdateRequest {
    @Size(max = 5000)
    private String content;
    private Integer pointsReward;
    private TaskStatus status;
    @Size(max = 500)
    private String aiResponse; // 备注/处理说明
}
