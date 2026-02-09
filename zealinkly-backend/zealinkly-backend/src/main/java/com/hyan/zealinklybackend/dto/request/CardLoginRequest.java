package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 卡片登录请求（身份证或社区卡）
 */
@Data
public class CardLoginRequest {
    
    @NotBlank(message = "用户类型不能为空")
    private String userType; // ELDER, VOLUNTEER
    
    @NotBlank(message = "图片不能为空")
    private String imageBase64; // 图片base64编码
    
    private String cardType; // ID_CARD, COMMUNITY_CARD，默认自动识别
}
