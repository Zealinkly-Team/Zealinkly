package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminElderCreateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100)
    private String password;

    private String realName;
    private String phone;
    private String address;
    @Size(max = 18)
    private String idCardNumber;
    @Size(max = 50)
    private String communityCardNumber;
    private BigDecimal lat;
    private BigDecimal lng;
}
