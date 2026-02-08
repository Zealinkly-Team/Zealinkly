package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestParam Long elderId, @RequestParam String question) {
        try {
            String answer = aiChatService.askAi(elderId, question);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("AI 暂时休息了，请稍后再试: " + e.getMessage());
        }
    }
}