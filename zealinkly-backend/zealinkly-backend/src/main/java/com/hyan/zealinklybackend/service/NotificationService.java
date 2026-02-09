package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.entity.Notification;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知服务（用户端）
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 获取用户的通知列表（按时间倒序）
     */
    public List<Notification> getNotifications(String receiverType, Long receiverId) {
        return notificationRepository.findByReceiverTypeAndReceiverIdOrderByCreatedAtDesc(
                receiverType, receiverId);
    }

    /**
     * 获取未读通知数量
     */
    public long getUnreadCount(String receiverType, Long receiverId) {
        return notificationRepository.countByReceiverTypeAndReceiverIdAndIsReadFalse(
                receiverType, receiverId);
    }

    /**
     * 标记单条通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId, String receiverType, Long receiverId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("通知不存在"));
        
        // 验证权限：只能标记自己的通知
        if (!notification.getReceiverType().equals(receiverType) ||
            !notification.getReceiverId().equals(receiverId)) {
            throw new BusinessException("无权操作此通知");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public void markAllAsRead(String receiverType, Long receiverId) {
        List<Notification> notifications = notificationRepository
                .findByReceiverTypeAndReceiverIdAndIsReadFalse(receiverType, receiverId);
        
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }
}
