package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.request.AgentRequest;
import com.hyan.zealinklybackend.dto.response.AgentResponse;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Agent统一入口控制器
 * 接收用户输入，自动识别意图并执行相应操作
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    /**
     * Agent统一入口：处理用户输入，自动识别意图并执行相应操作
     * 
     * 功能：
     * - 如果是互助任务：自动发布任务
     * - 如果是紧急报警：自动触发紧急报警
     * - 如果是AI聊天：自动开始AI对话
     */
    @PostMapping("/process")
    public ApiResponse<AgentResponse> process(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AgentRequest request) {
        
        // 仅老人可以使用Agent
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可使用Agent助手");
        }
        
        log.info("Agent processing request from elder {}: {}", principal.getUserId(), request.getUserInput());
        
        try {
            AgentResponse response = agentService.processUserInput(
                    principal.getUserId(),
                    request.getUserInput()
            );
            
            return ApiResponse.success(response.getMessage(), response);
            
        } catch (Exception e) {
            log.error("Agent processing failed", e);
            return ApiResponse.error(500, "处理失败: " + e.getMessage());
        }
    }
}
