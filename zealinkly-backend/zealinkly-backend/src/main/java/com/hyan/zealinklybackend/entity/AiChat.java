package com.hyan.zealinklybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_chats")
@Data
public class AiChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "elder_id")
    private Elder elder; // 提问的老人

    @Column(columnDefinition = "TEXT")
    private String question; // 老人的问题

    @Column(columnDefinition = "TEXT")
    private String answer;   // AI的回答

    private LocalDateTime createdAt;
}