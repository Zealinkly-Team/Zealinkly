package com.hyan.zealinklybackend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 志愿者提交完成请求（含交接凭证）
 */
@Data
public class SubmitCompletionRequest {
    @Size(max = 500, message = "完成备注长度不能超过500")
    private String note;

    /** 任务凭证列表（照片/语音/位置等），至少一条建议上传 */
    @Valid
    private List<EvidenceItemRequest> evidences = new ArrayList<>();
}
