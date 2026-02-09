package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 扫描卡片请求（管理员扫描志愿者卡片）
 */
@Data
public class ScanCardRequest {
    
    @NotBlank(message = "图片不能为空")
    private String imageBase64; // 图片base64编码
    
    private String cardType; // ID_CARD, COMMUNITY_CARD，默认自动识别
}
