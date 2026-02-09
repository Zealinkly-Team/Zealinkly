package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 老人 AI 提问请求
 */
@Data
public class AiAskRequest {
    @NotBlank(message = "问题不能为空")
    @Size(max = 2000, message = "问题长度不能超过2000")
    private String question;
}
