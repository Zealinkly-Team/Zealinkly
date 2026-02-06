package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.dto.request.UpdateUserInfoRequest;
import com.hyan.zealinklybackend.dto.response.UserInfoResponse;
import com.hyan.zealinklybackend.entity.Admin;
import com.hyan.zealinklybackend.entity.Elder;
import com.hyan.zealinklybackend.entity.Volunteer;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.AdminRepository;
import com.hyan.zealinklybackend.repository.ElderRepository;
import com.hyan.zealinklybackend.repository.VolunteerRepository;
import com.hyan.zealinklybackend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final ElderRepository elderRepository;
    private final VolunteerRepository volunteerRepository;
    private final AdminRepository adminRepository;

    /**
     * 获取用户信息
     */
    public UserInfoResponse getUserInfo(UserPrincipal userPrincipal) {
        UserInfoResponse response = new UserInfoResponse();

        switch (userPrincipal.getUserType()) {
            case "ELDER":
                Elder elder = elderRepository.findById(userPrincipal.getUserId())
                        .orElseThrow(() -> new BusinessException("用户不存在"));
                response.setId(elder.getId());
                response.setUsername(elder.getUsername());
                response.setRealName(elder.getRealName());
                response.setPhone(elder.getPhone());
                response.setUserType("ELDER");
                response.setPoints(elder.getPoints());
                response.setAddress(elder.getAddress());
                response.setLat(elder.getLat());
                response.setLng(elder.getLng());
                response.setCreatedAt(elder.getCreatedAt());
                break;

            case "VOLUNTEER":
                Volunteer volunteer = volunteerRepository.findById(userPrincipal.getUserId())
                        .orElseThrow(() -> new BusinessException("用户不存在"));
                response.setId(volunteer.getId());
                response.setUsername(volunteer.getUsername());
                response.setRealName(volunteer.getRealName());
                response.setPhone(volunteer.getPhone());
                response.setUserType("VOLUNTEER");
                response.setPoints(volunteer.getPoints());
                response.setIdCardStatus(volunteer.getIdCardStatus());
                response.setCreatedAt(volunteer.getCreatedAt());
                break;

            case "ADMIN":
                Admin admin = adminRepository.findById(userPrincipal.getUserId())
                        .orElseThrow(() -> new BusinessException("用户不存在"));
                response.setId(admin.getId());
                response.setUsername(admin.getUsername());
                response.setRealName(admin.getRealName());
                response.setUserType("ADMIN");
                response.setRoleLevel(admin.getRoleLevel());
                response.setCreatedAt(admin.getCreatedAt());
                break;

            default:
                throw new BusinessException("无效的用户类型");
        }

        return response;
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public UserInfoResponse updateUserInfo(UserPrincipal userPrincipal, UpdateUserInfoRequest request) {
        switch (userPrincipal.getUserType()) {
            case "ELDER":
                Elder elder = elderRepository.findById(userPrincipal.getUserId())
                        .orElseThrow(() -> new BusinessException("用户不存在"));
                if (request.getRealName() != null) elder.setRealName(request.getRealName());
                if (request.getPhone() != null) elder.setPhone(request.getPhone());
                if (request.getAddress() != null) elder.setAddress(request.getAddress());
                if (request.getLat() != null) elder.setLat(request.getLat());
                if (request.getLng() != null) elder.setLng(request.getLng());
                elderRepository.save(elder);
                break;

            case "VOLUNTEER":
                Volunteer volunteer = volunteerRepository.findById(userPrincipal.getUserId())
                        .orElseThrow(() -> new BusinessException("用户不存在"));
                if (request.getRealName() != null) volunteer.setRealName(request.getRealName());
                if (request.getPhone() != null) volunteer.setPhone(request.getPhone());
                volunteerRepository.save(volunteer);
                break;

            case "ADMIN":
                Admin admin = adminRepository.findById(userPrincipal.getUserId())
                        .orElseThrow(() -> new BusinessException("用户不存在"));
                if (request.getRealName() != null) admin.setRealName(request.getRealName());
                if (request.getRoleLevel() != null) admin.setRoleLevel(request.getRoleLevel());
                adminRepository.save(admin);
                break;

            default:
                throw new BusinessException("无效的用户类型");
        }

        return getUserInfo(userPrincipal);
    }
}
