package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.dto.request.AdminTaskUpdateRequest;
import com.hyan.zealinklybackend.dto.response.EvidenceItemResponse;
import com.hyan.zealinklybackend.dto.response.PointsLedgerItemResponse;
import com.hyan.zealinklybackend.dto.response.TaskDetailAdminResponse;
import com.hyan.zealinklybackend.dto.response.TaskResponse;
import com.hyan.zealinklybackend.entity.*;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTaskService {

    private final TaskRepository taskRepository;
    private final TaskEvidenceRepository taskEvidenceRepository;
    private final PointsLedgerRepository pointsLedgerRepository;

    /** 管理员：分页查询全部任务（按创建时间倒序） */
    @Transactional(readOnly = true)
    public Page<TaskResponse> listAll(Pageable pageable) {
        Page<Task> page = taskRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<TaskResponse> content = page.getContent().stream()
                .map(task -> {
                    Task loaded = taskRepository.findByIdWithAssociations(task.getId()).orElse(task);
                    return TaskResponse.fromEntity(loaded);
                })
                .collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(content, pageable, page.getTotalElements());
    }

    /** 管理员：根据志愿者ID查询任务（分页） */
    @Transactional(readOnly = true)
    public Page<TaskResponse> listByVolunteer(Long volunteerId, Pageable pageable) {
        List<Task> tasks = taskRepository.findByTaskTypeAndVolunteerIdOrderByCreatedAtDesc(
                com.hyan.zealinklybackend.entity.TaskType.COOPERATION, volunteerId);
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), tasks.size());
        List<Task> pagedTasks = start < tasks.size() ? tasks.subList(start, end) : new ArrayList<>();
        
        List<TaskResponse> content = pagedTasks.stream()
                .map(task -> {
                    Task loaded = taskRepository.findByIdWithAssociations(task.getId()).orElse(task);
                    return TaskResponse.fromEntity(loaded);
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, tasks.size());
    }

    /** 管理员：任务详情（含凭证、积分流水） */
    @Transactional(readOnly = true)
    public TaskDetailAdminResponse getDetail(Long taskId) {
        Task task = taskRepository.findByIdWithAssociations(taskId).orElseThrow(() -> new BusinessException("任务不存在"));
        List<TaskEvidence> evidences = taskEvidenceRepository.findByTaskIdOrderByCreatedAtAsc(task.getId());
        List<PointsLedger> ledgers = pointsLedgerRepository.findByTask_IdOrderByCreatedAtAsc(task.getId());
        return TaskDetailAdminResponse.builder()
                .task(TaskResponse.fromEntity(task))
                .evidenceList(evidences.stream().map(EvidenceItemResponse::fromEntity).collect(Collectors.toList()))
                .pointsLedgerList(ledgers.stream().map(PointsLedgerItemResponse::fromEntity).collect(Collectors.toList()))
                .build();
    }

    /** 管理员：编辑/审核任务（内容、积分、状态、备注） */
    @Transactional
    public TaskResponse update(Long taskId, AdminTaskUpdateRequest request) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new BusinessException("任务不存在"));
        if (request.getContent() != null) task.setContent(request.getContent());
        if (request.getPointsReward() != null) task.setPointsReward(request.getPointsReward());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getAiResponse() != null) task.setAiResponse(request.getAiResponse());
        task = taskRepository.save(task);
        task = taskRepository.findByIdWithAssociations(task.getId()).orElse(task);
        return TaskResponse.fromEntity(task);
    }
}
