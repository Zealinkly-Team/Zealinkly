package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitAppealRequest {
    @NotBlank(message = "申诉内容不能为空")
    @Size(max = 2000, message = "申诉内容长度不能超过2000")
    private String content;
}
