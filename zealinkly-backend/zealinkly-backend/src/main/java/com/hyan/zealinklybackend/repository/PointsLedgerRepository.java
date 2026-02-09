package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.PointsLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsLedgerRepository extends JpaRepository<PointsLedger, Long> {

    List<PointsLedger> findByUserTypeAndUserIdOrderByCreatedAtDesc(String userType, Long userId);

    List<PointsLedger> findByTask_IdOrderByCreatedAtAsc(Long taskId);
}
