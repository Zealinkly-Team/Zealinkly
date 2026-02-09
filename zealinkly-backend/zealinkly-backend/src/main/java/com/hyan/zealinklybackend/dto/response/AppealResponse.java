package com.hyan.zealinklybackend.dto.response;

import com.hyan.zealinklybackend.entity.Appeal;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class AppealResponse {
    private Long id;
    private Long taskId;
    private String complainantType;
    private Long complainantId;
    private String content;
    private String status;
    private String adminNote;
    private OffsetDateTime resolvedAt;
    private OffsetDateTime createdAt;

    public static AppealResponse fromEntity(Appeal a) {
        if (a == null) return null;
        return AppealResponse.builder()
                .id(a.getId())
                .taskId(a.getTask() != null ? a.getTask().getId() : null)
                .complainantType(a.getComplainantType())
                .complainantId(a.getComplainantId())
                .content(a.getContent())
                .status(a.getStatus())
                .adminNote(a.getAdminNote())
                .resolvedAt(a.getResolvedAt())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
