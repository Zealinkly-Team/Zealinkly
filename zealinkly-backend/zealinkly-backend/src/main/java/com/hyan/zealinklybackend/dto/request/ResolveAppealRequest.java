package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResolveAppealRequest {
    @Size(max = 1000, message = "处理备注长度不能超过1000")
    private String adminNote;
}
