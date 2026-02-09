package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Agent统一入口请求
 */
@Data
public class AgentRequest {
    
    @NotBlank(message = "用户输入不能为空")
    private String userInput;
}
