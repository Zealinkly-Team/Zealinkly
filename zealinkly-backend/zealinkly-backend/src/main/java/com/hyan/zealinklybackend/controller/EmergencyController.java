package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    // 老人端调用：一键报警
    // POST /api/emergency/trigger?elderId=1&location=春晖社区3号楼
    @PostMapping("/trigger")
    public ResponseEntity<?> trigger(@RequestParam Long elderId, @RequestParam String location) {
        emergencyService.triggerAlarm(elderId, location);
        return ResponseEntity.ok("报警已发出，救援正在路上");
    }

    // 管理端调用：标记已处理
    @PatchMapping("/{id}/handle")
    public ResponseEntity<?> handle(@PathVariable Long id, @RequestParam String note) {
        emergencyService.handleAlarm(id, note);
        return ResponseEntity.ok("处理成功");
    }
}