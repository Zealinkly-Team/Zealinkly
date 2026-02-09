package com.hyan.zealinklybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 紧急报警详情响应（包含老人信息、紧急联系人、定位信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyDetailResponse {
    /**
     * 任务基本信息
     */
    private TaskResponse task;
    
    /**
     * 老人基本信息
     */
    private ElderInfo elderInfo;
    
    /**
     * 紧急联系人列表
     */
    private List<EmergencyContactResponse> emergencyContacts;
    
    /**
     * 定位信息
     */
    private LocationInfo location;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ElderInfo {
        private Long id;
        private String username;
        private String realName;
        private String phone;
        private String address;
        private String idCardNumber;
        private String communityCardNumber;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        private BigDecimal lat;
        private BigDecimal lng;
        private String address;
        private String displayText; // 用于显示的定位文本
    }
}
