package com.hyan.zealinklybackend.dto.response;

import com.hyan.zealinklybackend.entity.EvidenceType;
import com.hyan.zealinklybackend.entity.TaskEvidence;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class EvidenceItemResponse {
    private Long id;
    private EvidenceType evidenceType;
    private String fileUrl;
    private OffsetDateTime createdAt;

    public static EvidenceItemResponse fromEntity(TaskEvidence e) {
        if (e == null) return null;
        return EvidenceItemResponse.builder()
                .id(e.getId())
                .evidenceType(e.getEvidenceType())
                .fileUrl(e.getFileUrl())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
