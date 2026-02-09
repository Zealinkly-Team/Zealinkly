package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员处理紧急报警请求
 */
@Data
public class HandleEmergencyRequest {
    @Size(max = 500, message = "处理备注长度不能超过500")
    private String note;
}
