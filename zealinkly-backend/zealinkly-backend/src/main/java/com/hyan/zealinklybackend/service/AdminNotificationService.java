package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.entity.Elder;
import com.hyan.zealinklybackend.entity.Notification;
import com.hyan.zealinklybackend.entity.Volunteer;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.ElderRepository;
import com.hyan.zealinklybackend.repository.NotificationRepository;
import com.hyan.zealinklybackend.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;
    private final ElderRepository elderRepository;
    private final VolunteerRepository volunteerRepository;

    /**
     * 广播弹窗：向全部老人或全部志愿者发送应用内通知
     * targetType: ALL_ELDERS / ALL_VOLUNTEERS
     */
    @Transactional
    public int broadcast(String targetType, String title, String message) {
        List<Notification> list = new ArrayList<>();
        if ("ALL_ELDERS".equalsIgnoreCase(targetType)) {
            for (Elder e : elderRepository.findAll()) {
                list.add(Notification.builder()
                        .receiverType("ELDER")
                        .receiverId(e.getId())
                        .title(title != null ? title : "系统通知")
                        .message(message)
                        .isRead(false)
                        .build());
            }
        } else if ("ALL_VOLUNTEERS".equalsIgnoreCase(targetType)) {
            for (Volunteer v : volunteerRepository.findAll()) {
                list.add(Notification.builder()
                        .receiverType("VOLUNTEER")
                        .receiverId(v.getId())
                        .title(title != null ? title : "系统通知")
                        .message(message)
                        .isRead(false)
                        .build());
            }
        } else {
            throw new BusinessException("目标类型无效，请使用 ALL_ELDERS 或 ALL_VOLUNTEERS");
        }
        if (list.isEmpty()) {
            return 0;
        }
        notificationRepository.saveAll(list);
        return list.size();
    }
}
