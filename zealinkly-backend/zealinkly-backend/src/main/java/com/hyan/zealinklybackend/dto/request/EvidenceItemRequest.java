package com.hyan.zealinklybackend.dto.request;

import com.hyan.zealinklybackend.entity.EvidenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 单条任务凭证（志愿者提交完成时上传）
 */
@Data
public class EvidenceItemRequest {
    @NotNull(message = "凭证类型不能为空")
    private EvidenceType evidenceType;

    @Size(max = 2000, message = "文件地址长度不能超过2000")
    private String fileUrl;
}
