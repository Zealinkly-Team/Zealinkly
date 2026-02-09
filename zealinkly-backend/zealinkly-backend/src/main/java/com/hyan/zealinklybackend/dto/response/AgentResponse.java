package com.hyan.zealinklybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agent统一入口响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    
    /**
     * 识别到的基础意图类型
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
     * 提取的任务清单
     */
    private List<TodoItem> tasks;
    
    /**
     * 处理结果
     */
    private String message;
    
    /**
     * 创建的任务列表（如果是互助任务或紧急报警）
     */
    private List<TaskResponse> createdTasks;
    
    /**
     * AI聊天回复（如果是AI聊天）
     */
    private String aiResponse;
}
