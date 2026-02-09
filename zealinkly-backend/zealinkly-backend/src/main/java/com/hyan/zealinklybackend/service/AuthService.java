package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.dto.request.CardLoginRequest;
import com.hyan.zealinklybackend.dto.request.LoginRequest;
import com.hyan.zealinklybackend.dto.request.RegisterRequest;
import com.hyan.zealinklybackend.dto.response.LoginResponse;
import com.hyan.zealinklybackend.dto.response.RegisterResponse;
import com.hyan.zealinklybackend.entity.Admin;
import com.hyan.zealinklybackend.entity.Elder;
import com.hyan.zealinklybackend.entity.Volunteer;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.AdminRepository;
import com.hyan.zealinklybackend.repository.ElderRepository;
import com.hyan.zealinklybackend.repository.VolunteerRepository;
import com.hyan.zealinklybackend.security.JwtTokenProvider;
import com.hyan.zealinklybackend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final ElderRepository elderRepository;
    private final VolunteerRepository volunteerRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OcrService ocrService;

    @Value("${app.points.initial-elder-points:0}")
    private int initialElderPoints;

    @Value("${app.points.initial-volunteer-points:0}")
    private int initialVolunteerPoints;

    /**
     * 检查用户名是否已存在（跨三张表）
     */
    private boolean usernameExists(String username) {
        return elderRepository.existsByUsername(username) ||
               volunteerRepository.existsByUsername(username) ||
               adminRepository.existsByUsername(username);
    }

    /**
     * 注册老人
     */
    @Transactional
    public RegisterResponse registerElder(RegisterRequest request) {
        if (usernameExists(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        Elder elder = new Elder();
        elder.setUsername(request.getUsername());
        elder.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        elder.setRealName(request.getRealName());
        elder.setPhone(request.getPhone());
        elder.setAddress(request.getAddress());
        elder.setLat(request.getLat());
        elder.setLng(request.getLng());
        elder.setPoints(initialElderPoints);

        Elder saved = elderRepository.save(elder);
        return new RegisterResponse(saved.getId(), saved.getUsername(), "ELDER");
    }

    /**
     * 注册志愿者
     */
    @Transactional
    public RegisterResponse registerVolunteer(RegisterRequest request) {
        if (usernameExists(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        Volunteer volunteer = new Volunteer();
        volunteer.setUsername(request.getUsername());
        volunteer.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        volunteer.setRealName(request.getRealName());
        volunteer.setPhone(request.getPhone());
        volunteer.setPoints(initialVolunteerPoints);

        Volunteer saved = volunteerRepository.save(volunteer);
        return new RegisterResponse(saved.getId(), saved.getUsername(), "VOLUNTEER");
    }

    /**
     * 注册管理员
     */
    @Transactional
    public RegisterResponse registerAdmin(RegisterRequest request) {
        if (usernameExists(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setRealName(request.getRealName());
        admin.setRoleLevel(request.getRoleLevel() != null ? request.getRoleLevel() : 1);

        Admin saved = adminRepository.save(admin);
        return new RegisterResponse(saved.getId(), saved.getUsername(), "ADMIN");
    }

    /**
     * 登录（只读事务，避免懒加载 no session）
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserPrincipal userPrincipal = null;
        Long userId = null;

        switch (request.getUserType().toUpperCase()) {
            case "ELDER":
                Elder elder = elderRepository.findByUsername(request.getUsername())
                        .orElseThrow(() -> new BusinessException("用户名或密码错误"));
                if (Boolean.FALSE.equals(elder.getEnabled())) {
                    throw new BusinessException("账号已被禁用，请联系管理员");
                }
                if (!passwordEncoder.matches(request.getPassword(), elder.getPasswordHash())) {
                    throw new BusinessException("用户名或密码错误");
                }
                userId = elder.getId();
                userPrincipal = new UserPrincipal("ELDER", userId, elder.getUsername());
                break;

            case "VOLUNTEER":
                Volunteer volunteer = volunteerRepository.findByUsername(request.getUsername())
                        .orElseThrow(() -> new BusinessException("用户名或密码错误"));
                if (Boolean.FALSE.equals(volunteer.getEnabled())) {
                    throw new BusinessException("账号已被禁用，请联系管理员");
                }
                if (!passwordEncoder.matches(request.getPassword(), volunteer.getPasswordHash())) {
                    throw new BusinessException("用户名或密码错误");
                }
                userId = volunteer.getId();
                userPrincipal = new UserPrincipal("VOLUNTEER", userId, volunteer.getUsername());
                break;

            case "ADMIN":
                Admin admin = adminRepository.findByUsername(request.getUsername())
                        .orElseThrow(() -> new BusinessException("用户名或密码错误"));
                if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
                    throw new BusinessException("用户名或密码错误");
                }
                userId = admin.getId();
                userPrincipal = new UserPrincipal("ADMIN", userId, admin.getUsername());
                break;

            default:
                throw new BusinessException("无效的用户类型");
        }

        String token = jwtTokenProvider.generateToken(userPrincipal);
        return new LoginResponse(token, userPrincipal.getUserType(), userId, userPrincipal.getUsername());
    }

    /**
     * 卡片登录（身份证或社区卡）
     */
    @Transactional(readOnly = true)
    public LoginResponse loginByCard(CardLoginRequest request) {
        String cardNumber;
        String cardType = request.getCardType() != null ? request.getCardType().toUpperCase() : null;

        // OCR识别
        if ("ID_CARD".equals(cardType)) {
            // 身份证识别
            cardNumber = ocrService.recognizeIdCard(request.getImageBase64());
        } else if ("COMMUNITY_CARD".equals(cardType)) {
            // 社区卡识别（通用文字识别）
            String text = ocrService.recognizeGeneralText(request.getImageBase64());
            cardNumber = ocrService.extractCardNumber(text);
            if (cardNumber.isEmpty()) {
                throw new BusinessException("未能识别到社区卡号");
            }
        } else {
            // 自动识别：先尝试身份证，失败则尝试社区卡
            try {
                cardNumber = ocrService.recognizeIdCard(request.getImageBase64());
                cardType = "ID_CARD";
            } catch (Exception e) {
                String text = ocrService.recognizeGeneralText(request.getImageBase64());
                cardNumber = ocrService.extractCardNumber(text);
                if (cardNumber.isEmpty()) {
                    throw new BusinessException("未能识别到卡片信息，请确保图片清晰");
                }
                cardType = "COMMUNITY_CARD";
            }
        }

        // 根据用户类型和卡号查找用户
        UserPrincipal userPrincipal = null;
        Long userId = null;
        String userType = request.getUserType().toUpperCase();

        if ("ELDER".equals(userType)) {
            Elder elder = null;
            if ("ID_CARD".equals(cardType)) {
                elder = elderRepository.findByIdCardNumber(cardNumber)
                        .orElse(null);
            } else {
                elder = elderRepository.findByCommunityCardNumber(cardNumber)
                        .orElse(null);
            }

            if (elder == null) {
                throw new BusinessException("未找到对应的用户，请先注册或联系管理员");
            }
            if (Boolean.FALSE.equals(elder.getEnabled())) {
                throw new BusinessException("账号已被禁用，请联系管理员");
            }
            userId = elder.getId();
            userPrincipal = new UserPrincipal("ELDER", userId, elder.getUsername());

        } else if ("VOLUNTEER".equals(userType)) {
            Volunteer volunteer = null;
            if ("ID_CARD".equals(cardType)) {
                volunteer = volunteerRepository.findByIdCardNumber(cardNumber)
                        .orElse(null);
            } else {
                volunteer = volunteerRepository.findByCommunityCardNumber(cardNumber)
                        .orElse(null);
            }

            if (volunteer == null) {
                throw new BusinessException("未找到对应的用户，请先注册或联系管理员");
            }
            if (Boolean.FALSE.equals(volunteer.getEnabled())) {
                throw new BusinessException("账号已被禁用，请联系管理员");
            }
            userId = volunteer.getId();
            userPrincipal = new UserPrincipal("VOLUNTEER", userId, volunteer.getUsername());

        } else {
            throw new BusinessException("卡片登录仅支持老人和志愿者");
        }

        String token = jwtTokenProvider.generateToken(userPrincipal);
        return new LoginResponse(token, userType, userId, userPrincipal.getUsername());
    }
}
