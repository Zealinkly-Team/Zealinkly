package com.hyan.zealinklybackend.entity;

public enum TaskStatus {
    PLACED,    // 老人已发布，等待志愿者领取
    TAKEN,     // 志愿者已接单，正在进行中
    COMPLETED, // 任务已完成
    CANCELED   // 任务已取消
}