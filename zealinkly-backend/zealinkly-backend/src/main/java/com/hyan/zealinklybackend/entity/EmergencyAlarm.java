package com.hyan.zealinklybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alarms")
@Data
public class EmergencyAlarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "elder_id", nullable = false)
    private Elder elder; // 关联报警的老人

    private String location; // 报警时的经纬度或地址描述

    private String status; // 状态: PENDING(待处理), HANDLED(已处理)

    private LocalDateTime createdAt;

    private LocalDateTime handledAt;

    private String handleNote; // 处理备注
}