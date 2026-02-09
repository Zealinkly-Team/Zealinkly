package com.hyan.zealinklybackend.entity;

/**
 * 统一任务状态（与 schema tasks.status 一致）
 */
public enum TaskStatus {
    PENDING,     // 待处理/待接单
    CLAIMED,     // 已接单/已认领
    IN_PROGRESS, // 进行中
    SUBMITTED,   // 已提交（待确认）
    COMPLETED,   // 已完成
    CANCELLED    // 已取消
}
