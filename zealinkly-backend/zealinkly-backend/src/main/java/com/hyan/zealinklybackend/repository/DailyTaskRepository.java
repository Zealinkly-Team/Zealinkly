package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.DailyTask;
import com.hyan.zealinklybackend.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> {
    // 寻找所有等待领取的任务
    List<DailyTask> findByStatus(TaskStatus status);

    // 寻找某个老人发布的所有任务
    List<DailyTask> findByElderId(Long elderId);

    // 寻找某个志愿者接下的所有任务
    List<DailyTask> findByVolunteerId(Long volunteerId);
}