package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建紧急联系人请求
 */
@Data
public class EmergencyContactCreateRequest {
    
    @NotBlank(message = "联系人姓名不能为空")
    private String name;
    
    private String relation; // 关系（如：儿子、女儿、朋友等）
    
    @NotBlank(message = "联系人电话不能为空")
    private String phone;
    
    @NotNull(message = "优先级不能为空")
    private Integer priority = 1; // 优先级，数字越小优先级越高
}
