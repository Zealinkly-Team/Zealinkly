package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.dto.request.AdminElderCreateRequest;
import com.hyan.zealinklybackend.dto.request.AdminElderUpdateRequest;
import com.hyan.zealinklybackend.dto.response.BulkImportResult;
import com.hyan.zealinklybackend.entity.PointsLedger;
import com.hyan.zealinklybackend.dto.response.ElderDetailResponse;
import com.hyan.zealinklybackend.entity.Elder;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.ElderRepository;
import com.hyan.zealinklybackend.repository.PointsLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminElderService {

    private final ElderRepository elderRepository;
    private final PointsLedgerRepository pointsLedgerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.points.initial-elder-points:0}")
    private int initialElderPoints;

    public Page<ElderDetailResponse> list(Boolean enabled, Pageable pageable) {
        Page<Elder> page = enabled != null
                ? elderRepository.findByEnabled(enabled, pageable)
                : elderRepository.findAll(pageable);
        return page.map(ElderDetailResponse::fromEntity);
    }

    public ElderDetailResponse getById(Long id) {
        Elder elder = elderRepository.findById(id).orElseThrow(() -> new BusinessException("老人不存在"));
        return ElderDetailResponse.fromEntity(elder);
    }

    @Transactional
    public ElderDetailResponse create(AdminElderCreateRequest request) {
        if (elderRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        Elder elder = new Elder();
        elder.setUsername(request.getUsername());
        elder.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        elder.setRealName(request.getRealName());
        elder.setPhone(request.getPhone());
        elder.setAddress(request.getAddress());
        elder.setIdCardNumber(request.getIdCardNumber());
        elder.setCommunityCardNumber(request.getCommunityCardNumber());
        elder.setLat(request.getLat());
        elder.setLng(request.getLng());
        elder.setPoints(initialElderPoints);
        elder.setEnabled(true);
        elder = elderRepository.save(elder);
        return ElderDetailResponse.fromEntity(elder);
    }

    @Transactional
    public ElderDetailResponse update(Long id, AdminElderUpdateRequest request) {
        Elder elder = elderRepository.findById(id).orElseThrow(() -> new BusinessException("老人不存在"));
        if (request.getRealName() != null) elder.setRealName(request.getRealName());
        if (request.getPhone() != null) elder.setPhone(request.getPhone());
        if (request.getAddress() != null) elder.setAddress(request.getAddress());
        if (request.getIdCardNumber() != null) elder.setIdCardNumber(request.getIdCardNumber());
        if (request.getCommunityCardNumber() != null) elder.setCommunityCardNumber(request.getCommunityCardNumber());
        if (request.getLat() != null) elder.setLat(request.getLat());
        if (request.getLng() != null) elder.setLng(request.getLng());
        if (request.getPoints() != null) elder.setPoints(request.getPoints());
        if (request.getEnabled() != null) elder.setEnabled(request.getEnabled());
        elder = elderRepository.save(elder);
        return ElderDetailResponse.fromEntity(elder);
    }

    @Transactional
    public void delete(Long id) {
        if (!elderRepository.existsById(id)) {
            throw new BusinessException("老人不存在");
        }
        elderRepository.deleteById(id);
    }

    @Transactional
    public void bulkDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        elderRepository.deleteAllById(ids);
    }

    @Transactional
    public ElderDetailResponse disable(Long id) {
        Elder elder = elderRepository.findById(id).orElseThrow(() -> new BusinessException("老人不存在"));
        elder.setEnabled(false);
        elder = elderRepository.save(elder);
        return ElderDetailResponse.fromEntity(elder);
    }

    @Transactional
    public ElderDetailResponse enable(Long id) {
        Elder elder = elderRepository.findById(id).orElseThrow(() -> new BusinessException("老人不存在"));
        elder.setEnabled(true);
        elder = elderRepository.save(elder);
        return ElderDetailResponse.fromEntity(elder);
    }

    /** 管理员给老人发放积分（记入流水） */
    @Transactional
    public ElderDetailResponse grantPoints(Long id, int amount) {
        if (amount <= 0) {
            throw new BusinessException("发放积分必须大于 0");
        }
        Elder elder = elderRepository.findById(id).orElseThrow(() -> new BusinessException("老人不存在"));
        int before = elder.getPoints() != null ? elder.getPoints() : 0;
        int after = before + amount;
        elder.setPoints(after);
        elderRepository.save(elder);
        PointsLedger ledger = PointsLedger.builder()
                .userType("ELDER")
                .userId(elder.getId())
                .amount(amount)
                .balanceAfter(after)
                .reason("ADMIN_GRANT")
                .build();
        pointsLedgerRepository.save(ledger);
        return ElderDetailResponse.fromEntity(elder);
    }

    /**
     * Excel 批量导入：第一行为表头，列顺序 用户名、密码、姓名、电话、地址、纬度、经度
     */
    @Transactional
    public BulkImportResult bulkImport(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请上传文件");
        }
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            int rowNum = 0;
            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue; // skip header
                if (row.getCell(0) == null || getCellString(row.getCell(0)).isBlank()) break;
                String username = getCellString(row.getCell(0)).trim();
                String password = getCellString(row.getCell(1)).trim();
                String realName = getCellString(row.getCell(2));
                String phone = getCellString(row.getCell(3));
                String address = getCellString(row.getCell(4));
                String latStr = getCellString(row.getCell(5));
                String lngStr = getCellString(row.getCell(6));
                if (username.length() < 3) {
                    errors.add("第" + rowNum + "行：用户名至少3位");
                    continue;
                }
                if (password.length() < 6) {
                    errors.add("第" + rowNum + "行：密码至少6位");
                    continue;
                }
                if (elderRepository.existsByUsername(username)) {
                    errors.add("第" + rowNum + "行：用户名已存在 " + username);
                    continue;
                }
                try {
                    Elder elder = new Elder();
                    elder.setUsername(username);
                    elder.setPasswordHash(passwordEncoder.encode(password));
                    elder.setRealName(realName.isEmpty() ? null : realName);
                    elder.setPhone(phone.isEmpty() ? null : phone);
                    elder.setAddress(address.isEmpty() ? null : address);
                    if (!latStr.isEmpty()) try { elder.setLat(new BigDecimal(latStr)); } catch (Exception ignored) {}
                    if (!lngStr.isEmpty()) try { elder.setLng(new BigDecimal(lngStr)); } catch (Exception ignored) {}
                    elder.setEnabled(true);
                    elderRepository.save(elder);
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + rowNum + "行：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new BusinessException("解析Excel失败: " + e.getMessage());
        }
        int failCount = errors.size();
        return BulkImportResult.builder().successCount(successCount).failCount(failCount).errors(errors).build();
    }

    private static String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
