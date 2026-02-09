package com.hyan.zealinklybackend.dto.response;

import com.hyan.zealinklybackend.entity.Elder;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class ElderDetailResponse {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String address;
    private Integer points;
    private BigDecimal lat;
    private BigDecimal lng;
    private Boolean enabled;
    private OffsetDateTime createdAt;

    public static ElderDetailResponse fromEntity(Elder e) {
        if (e == null) return null;
        return ElderDetailResponse.builder()
                .id(e.getId())
                .username(e.getUsername())
                .realName(e.getRealName())
                .phone(e.getPhone())
                .address(e.getAddress())
                .points(e.getPoints() != null ? e.getPoints() : 0)
                .lat(e.getLat())
                .lng(e.getLng())
                .enabled(e.getEnabled() != null ? e.getEnabled() : true)
                .createdAt(e.getCreatedAt())
                .build();
    }
}
