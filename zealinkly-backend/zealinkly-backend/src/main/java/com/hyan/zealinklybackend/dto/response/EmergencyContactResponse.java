package com.hyan.zealinklybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 紧急联系人响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactResponse {
    private Long id;
    private String name;
    private String relation;
    private String phone;
    private Integer priority;
}
