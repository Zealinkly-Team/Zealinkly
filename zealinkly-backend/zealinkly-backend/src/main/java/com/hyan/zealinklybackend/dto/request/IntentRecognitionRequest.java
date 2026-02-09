package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 意图识别请求
 */
@Data
public class IntentRecognitionRequest {
    
    @NotBlank(message = "用户输入不能为空")
    private String userInput; // 用户输入的一句话
}
