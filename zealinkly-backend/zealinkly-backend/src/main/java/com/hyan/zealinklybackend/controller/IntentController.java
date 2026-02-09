package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.request.IntentRecognitionRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.IntentRecognitionResponse;
import com.hyan.zealinklybackend.dto.response.TodoItem;
import com.hyan.zealinklybackend.service.intent.IntentRecognitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 意图识别控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/intent")
@RequiredArgsConstructor
public class IntentController {

    private final IntentRecognitionService intentRecognitionService;

    /**
     * 识别用户意图（所有文本都进行任务提取）
     */
    @PostMapping("/recognize")
    public ApiResponse<IntentRecognitionResponse> recognizeIntent(
            @Valid @RequestBody IntentRecognitionRequest request) {
        
        String userInput = request.getUserInput();
        log.info("Recognizing intent for user input (length: {}): {}", userInput.length(), userInput);
        
        try {
            // 所有文本都先尝试提取任务
            log.info("Extracting tasks from user input...");
            List<TodoItem> tasks = intentRecognitionService.extractTasks(userInput);
            
            String intentType;
            
            // 根据提取的任务确定基础意图
            if (tasks != null && !tasks.isEmpty()) {
                // 检查是否有紧急任务
                boolean hasEmergency = tasks.stream()
                        .anyMatch(task -> "EMERGENCY".equals(task.getType()));
                if (hasEmergency) {
                    intentType = "EMERGENCY";
                } else {
                    // 有互助任务
                    intentType = "MUTUAL_AID";
                }
            } else {
                // 没有提取到任务，使用简单意图识别作为后备
                log.info("No tasks extracted, falling back to simple intent recognition...");
                intentType = intentRecognitionService.recognizeIntent(userInput);
                intentType = intentType.trim().toUpperCase();
                
                // 验证和规范化意图类型
                if (!intentType.equals("MUTUAL_AID") && 
                    !intentType.equals("EMERGENCY") && 
                    !intentType.equals("AI_CHAT")) {
                    // 如果返回的不是标准格式，尝试从响应中提取
                    if (intentType.contains("MUTUAL") || intentType.contains("互助")) {
                        intentType = "MUTUAL_AID";
                    } else if (intentType.contains("EMERGENCY") || intentType.contains("紧急")) {
                        intentType = "EMERGENCY";
                    } else {
                        intentType = "AI_CHAT"; // 默认
                    }
                }
            }
            
            String description = switch (intentType) {
                case "MUTUAL_AID" -> "互助任务";
                case "EMERGENCY" -> "紧急报警";
                case "AI_CHAT" -> "AI聊天";
                default -> "未知";
            };
            
            IntentRecognitionResponse response = IntentRecognitionResponse.builder()
                    .intentType(intentType)
                    .intentDescription(description)
                    .userInput(userInput)
                    .tasks(tasks != null ? tasks : new java.util.ArrayList<>())
                    .build();
            
            log.info("Recognized intent: {} with {} tasks for input length: {}", 
                    intentType, tasks != null ? tasks.size() : 0, userInput.length());
            
            return ApiResponse.success("意图识别成功", response);
            
        } catch (Exception e) {
            log.error("Intent recognition failed", e);
            // 默认返回AI聊天
            IntentRecognitionResponse response = IntentRecognitionResponse.builder()
                    .intentType("AI_CHAT")
                    .intentDescription("AI聊天")
                    .userInput(userInput)
                    .tasks(new java.util.ArrayList<>())
                    .build();
            return ApiResponse.success("意图识别失败，默认返回AI聊天", response);
        }
    }
}
