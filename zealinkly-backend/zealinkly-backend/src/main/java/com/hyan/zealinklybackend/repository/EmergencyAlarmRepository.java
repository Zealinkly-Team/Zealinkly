package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.EmergencyAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmergencyAlarmRepository extends JpaRepository<EmergencyAlarm, Long> {
    List<EmergencyAlarm> findByStatus(String status);
}