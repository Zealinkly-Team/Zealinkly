package com.hyan.zealinklybackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 文件存储实体
 */
@Entity
@Table(name = "file_storage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文件类型：IMAGE, AUDIO, DOCUMENT等
     */
    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;

    /**
     * 原始文件名
     */
    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    /**
     * 存储的文件名（包含路径）
     */
    @Column(name = "stored_filename", nullable = false, length = 500)
    private String storedFilename;

    /**
     * 文件访问URL
     */
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * MIME类型
     */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * 文件扩展名
     */
    @Column(name = "file_extension", length = 20)
    private String fileExtension;

    /**
     * 上传用户类型：ELDER, VOLUNTEER, ADMIN
     */
    @Column(name = "uploader_type", length = 20)
    private String uploaderType;

    /**
     * 上传用户ID
     */
    @Column(name = "uploader_id")
    private Long uploaderId;

    /**
     * 关联的业务ID（如任务ID）
     */
    @Column(name = "related_id")
    private Long relatedId;

    /**
     * 关联的业务类型（如TASK, PROFILE等）
     */
    @Column(name = "related_type", length = 50)
    private String relatedType;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
