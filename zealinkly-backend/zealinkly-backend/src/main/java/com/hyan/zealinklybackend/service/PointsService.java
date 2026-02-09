package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.dto.response.PointsLedgerResponse;
import com.hyan.zealinklybackend.entity.Elder;
import com.hyan.zealinklybackend.entity.PointsLedger;
import com.hyan.zealinklybackend.entity.Volunteer;
import com.hyan.zealinklybackend.repository.ElderRepository;
import com.hyan.zealinklybackend.repository.PointsLedgerRepository;
import com.hyan.zealinklybackend.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分服务
 */
@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsLedgerRepository pointsLedgerRepository;
    private final ElderRepository elderRepository;
    private final VolunteerRepository volunteerRepository;

    /**
     * 获取用户的积分总数
     */
    @Transactional(readOnly = true)
    public Integer getPointsTotal(String userType, Long userId) {
        if ("ELDER".equals(userType)) {
            Elder elder = elderRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("老人不存在"));
            return elder.getPoints() != null ? elder.getPoints() : 0;
        } else if ("VOLUNTEER".equals(userType)) {
            Volunteer volunteer = volunteerRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("志愿者不存在"));
            return volunteer.getPoints() != null ? volunteer.getPoints() : 0;
        }
        return 0;
    }

    /**
     * 获取用户的积分流水记录
     */
    @Transactional(readOnly = true)
    public List<PointsLedgerResponse> getPointsHistory(String userType, Long userId) {
        List<PointsLedger> ledgers = pointsLedgerRepository.findByUserTypeAndUserIdOrderByCreatedAtDesc(userType, userId);
        
        return ledgers.stream()
                .map(ledger -> PointsLedgerResponse.builder()
                        .id(ledger.getId())
                        .amount(ledger.getAmount())
                        .balanceAfter(ledger.getBalanceAfter())
                        .reason(ledger.getReason())
                        .taskId(ledger.getTask() != null ? ledger.getTask().getId() : null)
                        .exchangeId(ledger.getExchange() != null ? ledger.getExchange().getId() : null)
                        .createdAt(ledger.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
