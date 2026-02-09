package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 老人发布互助任务请求
 */
@Data
public class PublishTaskRequest {
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @Size(max = 2000, message = "描述长度不能超过2000")
    private String description;

    /** 任务奖励积分，可选，默认 0 */
    private Integer pointsReward = 0;
}
