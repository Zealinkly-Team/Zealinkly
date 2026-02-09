package com.hyan.zealinklybackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * 任务凭证（志愿者提交完成时上传的照片/语音/位置等）
 */
@Entity
@Table(name = "task_evidence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false, length = 20)
    private EvidenceType evidenceType;

    @Column(name = "file_url", columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
