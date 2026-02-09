package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员广播通知：ALL_ELDERS / ALL_VOLUNTEERS
 */
@Data
public class BroadcastRequest {
    @NotBlank(message = "目标类型不能为空")
    private String targetType; // ALL_ELDERS, ALL_VOLUNTEERS

    @Size(max = 100, message = "标题长度不能超过100")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容长度不能超过2000")
    private String message;
}
