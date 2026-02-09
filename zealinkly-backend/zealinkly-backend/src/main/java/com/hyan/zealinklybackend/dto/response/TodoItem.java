package com.hyan.zealinklybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 待办任务项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoItem {
    
    /**
     * 任务类型：MUTUAL_AID（互助任务）、EMERGENCY（紧急报警）
     */
    private String type;
    
    /**
     * 任务类型的中文描述
     */
    private String typeDescription;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 优先级：HIGH（高）、MEDIUM（中）、LOW（低）
     */
    private String priority;
}
