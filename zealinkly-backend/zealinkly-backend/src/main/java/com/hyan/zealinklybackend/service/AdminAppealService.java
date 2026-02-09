package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.dto.response.AppealResponse;
import com.hyan.zealinklybackend.entity.Appeal;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.AppealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAppealService {

    private final AppealRepository appealRepository;

    public Page<AppealResponse> list(String status, Pageable pageable) {
        Page<Appeal> page = status != null && !status.isBlank()
                ? appealRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : appealRepository.findAllByOrderByCreatedAtDesc(pageable);
        return page.map(AppealResponse::fromEntity);
    }

    public List<AppealResponse> listPending() {
        return appealRepository.findByStatusOrderByCreatedAtDesc("PENDING").stream()
                .map(AppealResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public AppealResponse getById(Long id) {
        Appeal a = appealRepository.findById(id).orElseThrow(() -> new BusinessException("申诉不存在"));
        return AppealResponse.fromEntity(a);
    }

    @Transactional
    public AppealResponse resolve(Long id, String adminNote) {
        Appeal appeal = appealRepository.findById(id).orElseThrow(() -> new BusinessException("申诉不存在"));
        if ("RESOLVED".equals(appeal.getStatus())) {
            throw new BusinessException("该申诉已处理");
        }
        appeal.setStatus("RESOLVED");
        appeal.setAdminNote(adminNote);
        appeal.setResolvedAt(OffsetDateTime.now());
        appeal = appealRepository.save(appeal);
        return AppealResponse.fromEntity(appeal);
    }
}
