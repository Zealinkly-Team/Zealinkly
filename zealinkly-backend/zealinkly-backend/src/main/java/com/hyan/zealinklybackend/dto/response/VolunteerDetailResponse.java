package com.hyan.zealinklybackend.dto.response;

import com.hyan.zealinklybackend.entity.Volunteer;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class VolunteerDetailResponse {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private Integer points;
    private Boolean idCardStatus;
    private Boolean enabled;
    private OffsetDateTime createdAt;

    public static VolunteerDetailResponse fromEntity(Volunteer v) {
        if (v == null) return null;
        return VolunteerDetailResponse.builder()
                .id(v.getId())
                .username(v.getUsername())
                .realName(v.getRealName())
                .phone(v.getPhone())
                .points(v.getPoints() != null ? v.getPoints() : 0)
                .idCardStatus(v.getIdCardStatus() != null ? v.getIdCardStatus() : false)
                .enabled(v.getEnabled() != null ? v.getEnabled() : true)
                .createdAt(v.getCreatedAt())
                .build();
    }
}
