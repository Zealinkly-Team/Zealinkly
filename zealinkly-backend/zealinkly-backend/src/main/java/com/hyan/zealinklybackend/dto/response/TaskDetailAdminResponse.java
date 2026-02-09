package com.hyan.zealinklybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理员查看任务详情：任务信息 + 凭证列表 + 积分流水
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailAdminResponse {
    private TaskResponse task;
    private List<EvidenceItemResponse> evidenceList;
    private List<PointsLedgerItemResponse> pointsLedgerList;
}
