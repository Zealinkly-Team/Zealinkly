package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminVolunteerUpdateRequest {
    @Size(max = 50)
    private String realName;
    @Size(max = 20)
    private String phone;
    @Size(max = 18)
    private String idCardNumber;
    @Size(max = 50)
    private String communityCardNumber;
    private Integer points;
    private Boolean idCardStatus;
    private Boolean enabled;
}
