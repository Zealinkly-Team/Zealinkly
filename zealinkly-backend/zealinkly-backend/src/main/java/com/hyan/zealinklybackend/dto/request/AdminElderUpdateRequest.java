package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminElderUpdateRequest {
    @Size(max = 50)
    private String realName;
    @Size(max = 20)
    private String phone;
    private String address;
    @Size(max = 18)
    private String idCardNumber;
    @Size(max = 50)
    private String communityCardNumber;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer points;
    private Boolean enabled;
}
