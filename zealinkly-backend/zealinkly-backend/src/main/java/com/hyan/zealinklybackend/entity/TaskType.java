package com.hyan.zealinklybackend.entity;

/**
 * 统一任务类型（与 schema tasks.task_type 一致）
 */
public enum TaskType {
    EMERGENCY,   // 紧急报警
    COOPERATION, // 互助任务
    AI_CHAT,     // AI 聊天
    POLICY       // 政策咨询（预留）
}
