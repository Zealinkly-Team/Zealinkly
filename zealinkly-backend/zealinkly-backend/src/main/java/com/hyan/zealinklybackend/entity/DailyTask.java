package com.hyan.zealinklybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_tasks")
@Data
public class DailyTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;       // 任务标题 (例如: 帮买菜、陪聊天)

    @Column(columnDefinition = "TEXT")
    private String description; // 详细描述

    private String voiceUrl;    // 老人录音文件的存储路径 (留给语音功能用)

    @Enumerated(EnumType.STRING)
    private TaskStatus status;  // 任务状态

    @ManyToOne
    @JoinColumn(name = "elder_id", nullable = false)
    private Elder elder;        // 发布任务的老人

    @ManyToOne
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer; // 接单的志愿者 (初始为空)

    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
}