package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.dto.request.AdminVolunteerCreateRequest;
import com.hyan.zealinklybackend.dto.request.AdminVolunteerUpdateRequest;
import com.hyan.zealinklybackend.dto.response.BulkImportResult;
import com.hyan.zealinklybackend.dto.response.VolunteerDetailResponse;
import com.hyan.zealinklybackend.entity.PointsLedger;
import com.hyan.zealinklybackend.entity.Volunteer;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.PointsLedgerRepository;
import com.hyan.zealinklybackend.repository.VolunteerRepository;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminVolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final PasswordEncoder passwordEncoder;
    private final PointsLedgerRepository pointsLedgerRepository;

    @Value("${app.points.initial-volunteer-points:0}")
    private int initialVolunteerPoints;

    public Page<VolunteerDetailResponse> list(Boolean enabled, Pageable pageable) {
        Page<Volunteer> page = enabled != null
                ? volunteerRepository.findByEnabled(enabled, pageable)
                : volunteerRepository.findAll(pageable);
        return page.map(VolunteerDetailResponse::fromEntity);
    }

    public VolunteerDetailResponse getById(Long id) {
        Volunteer v = volunteerRepository.findById(id).orElseThrow(() -> new BusinessException("志愿者不存在"));
        return VolunteerDetailResponse.fromEntity(v);
    }

    @Transactional
    public VolunteerDetailResponse create(AdminVolunteerCreateRequest request) {
        if (volunteerRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        Volunteer v = new Volunteer();
        v.setUsername(request.getUsername());
        v.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        v.setRealName(request.getRealName());
        v.setPhone(request.getPhone());
        v.setIdCardNumber(request.getIdCardNumber());
        v.setCommunityCardNumber(request.getCommunityCardNumber());
        v.setPoints(initialVolunteerPoints);
        v.setEnabled(true);
        v = volunteerRepository.save(v);
        return VolunteerDetailResponse.fromEntity(v);
    }

    @Transactional
    public VolunteerDetailResponse update(Long id, AdminVolunteerUpdateRequest request) {
        Volunteer v = volunteerRepository.findById(id).orElseThrow(() -> new BusinessException("志愿者不存在"));
        if (request.getRealName() != null) v.setRealName(request.getRealName());
        if (request.getPhone() != null) v.setPhone(request.getPhone());
        if (request.getIdCardNumber() != null) v.setIdCardNumber(request.getIdCardNumber());
        if (request.getCommunityCardNumber() != null) v.setCommunityCardNumber(request.getCommunityCardNumber());
        if (request.getPoints() != null) v.setPoints(request.getPoints());
        if (request.getIdCardStatus() != null) v.setIdCardStatus(request.getIdCardStatus());
        if (request.getEnabled() != null) v.setEnabled(request.getEnabled());
        v = volunteerRepository.save(v);
        return VolunteerDetailResponse.fromEntity(v);
    }

    @Transactional
    public void delete(Long id) {
        if (!volunteerRepository.existsById(id)) {
            throw new BusinessException("志愿者不存在");
        }
        volunteerRepository.deleteById(id);
    }

    @Transactional
    public void bulkDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        volunteerRepository.deleteAllById(ids);
    }

    @Transactional
    public VolunteerDetailResponse disable(Long id) {
        Volunteer v = volunteerRepository.findById(id).orElseThrow(() -> new BusinessException("志愿者不存在"));
        v.setEnabled(false);
        v = volunteerRepository.save(v);
        return VolunteerDetailResponse.fromEntity(v);
    }

    @Transactional
    public VolunteerDetailResponse enable(Long id) {
        Volunteer v = volunteerRepository.findById(id).orElseThrow(() -> new BusinessException("志愿者不存在"));
        v.setEnabled(true);
        v = volunteerRepository.save(v);
        return VolunteerDetailResponse.fromEntity(v);
    }

    /** Excel 批量导入：第一行为表头，列顺序 用户名、密码、姓名、电话 */
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
                if (rowNum == 1) continue;
                if (row.getCell(0) == null || getCellString(row.getCell(0)).isBlank()) break;
                String username = getCellString(row.getCell(0)).trim();
                String password = getCellString(row.getCell(1)).trim();
                String realName = getCellString(row.getCell(2));
                String phone = getCellString(row.getCell(3));
                if (username.length() < 3) {
                    errors.add("第" + rowNum + "行：用户名至少3位");
                    continue;
                }
                if (password.length() < 6) {
                    errors.add("第" + rowNum + "行：密码至少6位");
                    continue;
                }
                if (volunteerRepository.existsByUsername(username)) {
                    errors.add("第" + rowNum + "行：用户名已存在 " + username);
                    continue;
                }
                try {
                    Volunteer v = new Volunteer();
                    v.setUsername(username);
                    v.setPasswordHash(passwordEncoder.encode(password));
                    v.setRealName(realName.isEmpty() ? null : realName);
                    v.setPhone(phone.isEmpty() ? null : phone);
                    v.setEnabled(true);
                    volunteerRepository.save(v);
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + rowNum + "行：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new BusinessException("解析Excel失败: " + e.getMessage());
        }
        return BulkImportResult.builder().successCount(successCount).failCount(errors.size()).errors(errors).build();
    }

    /** 管理员给志愿者发放积分（记入流水） */
    @Transactional
    public VolunteerDetailResponse grantPoints(Long id, int amount) {
        if (amount <= 0) {
            throw new BusinessException("发放积分必须大于 0");
        }
        Volunteer volunteer = volunteerRepository.findById(id).orElseThrow(() -> new BusinessException("志愿者不存在"));
        int before = volunteer.getPoints() != null ? volunteer.getPoints() : 0;
        int after = before + amount;
        volunteer.setPoints(after);
        volunteerRepository.save(volunteer);
        PointsLedger ledger = PointsLedger.builder()
                .userType("VOLUNTEER")
                .userId(volunteer.getId())
                .amount(amount)
                .balanceAfter(after)
                .reason("ADMIN_GRANT")
                .build();
        pointsLedgerRepository.save(ledger);
        return VolunteerDetailResponse.fromEntity(volunteer);
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
