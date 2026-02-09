package com.hyan.zealinklybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * AI 聊天单条记录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatItemResponse {
    private Long taskId;
    private String question;
    private String answer;
    private OffsetDateTime createdAt;
}
