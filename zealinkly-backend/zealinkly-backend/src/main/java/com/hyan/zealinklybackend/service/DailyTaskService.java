package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.entity.*;
import com.hyan.zealinklybackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyTaskService {

    private final DailyTaskRepository taskRepository;
    private final ElderRepository elderRepository;
    private final VolunteerRepository volunteerRepository;

    // 1. 老人发布任务
    public DailyTask createRawTask(Long elderId, String title, String desc, String voiceUrl) {
        Elder elder = elderRepository.findById(elderId).orElseThrow();

        DailyTask task = new DailyTask();
        task.setElder(elder);
        task.setTitle(title);
        task.setDescription(desc);
        task.setVoiceUrl(voiceUrl);
        task.setStatus(TaskStatus.PLACED);
        task.setCreatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    // 2. 志愿者领取任务 (抢单)
    public DailyTask acceptTask(Long taskId, Long volunteerId) {
        DailyTask task = taskRepository.findById(taskId).orElseThrow();

        if (task.getStatus() != TaskStatus.PLACED) {
            throw new RuntimeException("手慢了！该任务已被别人领取或已取消");
        }

        Volunteer volunteer = volunteerRepository.findById(volunteerId).orElseThrow();

        task.setVolunteer(volunteer);
        task.setStatus(TaskStatus.TAKEN);
        task.setAcceptedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    // 3. 志愿者完成任务
    public DailyTask completeTask(Long taskId) {
        DailyTask task = taskRepository.findById(taskId).orElseThrow();
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    // 4. 获取所有待领取的任务列表 (大厅)
    public List<DailyTask> getAvailableTasks() {
        return taskRepository.findByStatus(TaskStatus.PLACED);
    }
}