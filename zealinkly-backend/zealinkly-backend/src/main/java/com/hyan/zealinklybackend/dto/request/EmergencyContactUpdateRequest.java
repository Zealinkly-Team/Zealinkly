package com.hyan.zealinklybackend.dto.request;

import lombok.Data;

/**
 * 更新紧急联系人请求
 */
@Data
public class EmergencyContactUpdateRequest {
    
    private String name;
    
    private String relation;
    
    private String phone;
    
    private Integer priority;
}
