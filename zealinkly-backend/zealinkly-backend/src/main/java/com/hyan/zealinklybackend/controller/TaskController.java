package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.request.PublishTaskRequest;
import com.hyan.zealinklybackend.dto.request.SubmitAppealRequest;
import com.hyan.zealinklybackend.dto.request.SubmitCompletionRequest;
import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.TaskResponse;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 互助任务接口
 */
@RestController
@RequestMapping("/api/tasks/cooperation")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /** 老人：发布互助任务 */
    @PostMapping("/publish")
    public ApiResponse<TaskResponse> publish(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PublishTaskRequest request) {
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可发布互助任务");
        }
        TaskResponse resp = taskService.publishCooperation(
                principal.getUserId(),
                request.getTitle(),
                request.getDescription(),
                request.getPointsReward());
        return ApiResponse.success("发布成功", resp);
    }

    /** 志愿者：可接任务列表（任务大厅） */
    @GetMapping("/available")
    public ApiResponse<List<TaskResponse>> available(@AuthenticationPrincipal UserPrincipal principal) {
        List<TaskResponse> list = taskService.getAvailableCooperationTasks();
        return ApiResponse.success(list);
    }

    /** 志愿者：接单 */
    @PostMapping("/{taskId}/accept")
    public ApiResponse<TaskResponse> accept(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long taskId) {
        if (!"VOLUNTEER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅志愿者可接单");
        }
        TaskResponse resp = taskService.acceptCooperation(taskId, principal.getUserId());
        return ApiResponse.success("接单成功", resp);
    }

    /** 志愿者：开始服务（可选） */
    @PostMapping("/{taskId}/start")
    public ApiResponse<TaskResponse> start(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long taskId) {
        if (!"VOLUNTEER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅志愿者可操作");
        }
        TaskResponse resp = taskService.startCooperation(taskId, principal.getUserId());
        return ApiResponse.success("已开始服务", resp);
    }

    /** 志愿者：提交完成 + 上传凭证，等待老人确认交接 */
    @PostMapping("/{taskId}/submit")
    public ApiResponse<TaskResponse> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long taskId,
            @Valid @RequestBody SubmitCompletionRequest request) {
        if (!"VOLUNTEER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅志愿者可提交完成");
        }
        TaskResponse resp = taskService.volunteerSubmitCompletion(
                taskId,
                principal.getUserId(),
                request.getNote(),
                request.getEvidences() != null ? request.getEvidences() : Collections.emptyList());
        return ApiResponse.success("已提交完成，等待老人确认交接", resp);
    }

    /** 老人：确认交接，任务完成并结算积分 */
    @PostMapping("/{taskId}/confirm")
    public ApiResponse<TaskResponse> confirm(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long taskId) {
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可确认交接");
        }
        TaskResponse resp = taskService.elderConfirmCompletion(taskId, principal.getUserId());
        return ApiResponse.success("交接已确认，任务完成", resp);
    }

    /** 老人或志愿者：针对该任务提交申诉 */
    @PostMapping("/{taskId}/appeal")
    public ApiResponse<Void> appeal(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long taskId,
            @Valid @RequestBody SubmitAppealRequest request) {
        if (!"ELDER".equals(principal.getUserType()) && !"VOLUNTEER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人或志愿者可提交申诉");
        }
        taskService.submitAppeal(taskId, principal.getUserId(), principal.getUserType(), request.getContent());
        return ApiResponse.success("申诉已提交，请等待管理员处理", null);
    }

    /** 任务详情（含凭证），仅该任务的老人或志愿者可查看 */
    @GetMapping("/{taskId}")
    public ApiResponse<TaskResponse> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long taskId) {
        TaskResponse resp = taskService.getCooperationTaskDetail(taskId, principal.getUserId(), principal.getUserType());
        return ApiResponse.success(resp);
    }

    /** 老人：我发布的任务列表 */
    @GetMapping("/my-as-elder")
    public ApiResponse<List<TaskResponse>> myAsElder(@AuthenticationPrincipal UserPrincipal principal) {
        if (!"ELDER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅老人可查看自己发布的任务");
        }
        List<TaskResponse> list = taskService.getMyCooperationAsElder(principal.getUserId());
        return ApiResponse.success(list);
    }

    /** 志愿者：我接下的任务列表 */
    @GetMapping("/my-as-volunteer")
    public ApiResponse<List<TaskResponse>> myAsVolunteer(@AuthenticationPrincipal UserPrincipal principal) {
        if (!"VOLUNTEER".equals(principal.getUserType())) {
            return ApiResponse.error(403, "仅志愿者可查看自己接下的任务");
        }
        List<TaskResponse> list = taskService.getMyCooperationAsVolunteer(principal.getUserId());
        return ApiResponse.success(list);
    }
}
