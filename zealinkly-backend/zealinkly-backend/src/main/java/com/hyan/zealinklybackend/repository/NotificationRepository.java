package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverTypeAndReceiverIdOrderByCreatedAtDesc(String receiverType, Long receiverId, Pageable pageable);
    
    List<Notification> findByReceiverTypeAndReceiverIdOrderByCreatedAtDesc(String receiverType, Long receiverId);

    long countByReceiverTypeAndReceiverIdAndIsReadFalse(String receiverType, Long receiverId);
    
    List<Notification> findByReceiverTypeAndReceiverIdAndIsReadFalse(String receiverType, Long receiverId);
}
