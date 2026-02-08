package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.entity.DailyTask;
import com.hyan.zealinklybackend.service.DailyTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class DailyTaskController {

    private final DailyTaskService taskService;

    // 老人发布新需求
    @PostMapping("/publish")
    public ResponseEntity<DailyTask> publish(@RequestParam Long elderId,
                                             @RequestParam String title,
                                             @RequestParam(required = false) String desc) {
        return ResponseEntity.ok(taskService.createRawTask(elderId, title, desc, null));
    }

    // 志愿者查看任务大厅
    @GetMapping("/available")
    public List<DailyTask> getAvailable() {
        return taskService.getAvailableTasks();
    }

    // 志愿者接单
    @PostMapping("/{taskId}/accept")
    public ResponseEntity<DailyTask> accept(@PathVariable Long taskId, @RequestParam Long volunteerId) {
        return ResponseEntity.ok(taskService.acceptTask(taskId, volunteerId));
    }

    // 志愿者提交完成
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<DailyTask> complete(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.completeTask(taskId));
    }
}