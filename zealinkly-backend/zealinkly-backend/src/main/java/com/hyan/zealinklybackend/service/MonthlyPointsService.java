package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.entity.Elder;
import com.hyan.zealinklybackend.entity.PointsLedger;
import com.hyan.zealinklybackend.repository.ElderRepository;
import com.hyan.zealinklybackend.repository.PointsLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 每月 1 号 0 点给所有在籍老人发放积分
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyPointsService {

    private final ElderRepository elderRepository;
    private final PointsLedgerRepository pointsLedgerRepository;

    @Value("${app.points.monthly-elder-points:100}")
    private int monthlyPoints;

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void grantMonthlyPointsToElders() {
        if (monthlyPoints <= 0) {
            log.info("monthly-elder-points is 0, skip monthly grant");
            return;
        }
        List<Elder> elders = elderRepository.findByEnabledTrue();
        int count = 0;
        for (Elder elder : elders) {
            int before = elder.getPoints() != null ? elder.getPoints() : 0;
            int after = before + monthlyPoints;
            elder.setPoints(after);
            elderRepository.save(elder);
            PointsLedger ledger = PointsLedger.builder()
                    .userType("ELDER")
                    .userId(elder.getId())
                    .amount(monthlyPoints)
                    .balanceAfter(after)
                    .reason("MONTHLY_GRANT")
                    .build();
            pointsLedgerRepository.save(ledger);
            count++;
        }
        log.info("Monthly points granted: {} elders, {} points each, total {}", count, monthlyPoints, count * monthlyPoints);
    }
}
