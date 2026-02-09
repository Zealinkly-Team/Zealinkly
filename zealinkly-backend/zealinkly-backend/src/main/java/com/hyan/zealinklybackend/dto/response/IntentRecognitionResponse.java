package com.hyan.zealinklybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 意图识别响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentRecognitionResponse {
    
    /**
     * 识别到的基础意图类型
     * MUTUAL_AID - 互助任务
     * EMERGENCY - 紧急报警
     * AI_CHAT - AI聊天（默认，当用户说长段话时）
     */
    private String intentType;
    
    /**
     * 意图类型的中文描述
     */
    private String intentDescription;
    
    /**
     * 用户原始输入
     */
    private String userInput;
    
    /**
     * 从用户输入中提取的任务清单（待办事项）
     * 如果用户输入中包含互助任务或紧急报警，会提取到这里
     */
    private List<TodoItem> tasks;
}
