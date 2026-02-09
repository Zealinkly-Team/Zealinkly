package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 老人一键报警请求
 */
@Data
public class TriggerEmergencyRequest {
    @NotBlank(message = "位置信息不能为空")
    @Size(max = 500, message = "位置描述长度不能超过500")
    private String location;
}
