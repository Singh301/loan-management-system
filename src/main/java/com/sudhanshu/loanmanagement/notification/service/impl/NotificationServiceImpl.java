package com.sudhanshu.loanmanagement.notification.service.impl;

import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.notification.entity.Notification;
import com.sudhanshu.loanmanagement.notification.entity.Notification.NotificationStatus;
import com.sudhanshu.loanmanagement.notification.entity.Notification.NotificationType;
import com.sudhanshu.loanmanagement.notification.repository.NotificationRepository;
import com.sudhanshu.loanmanagement.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void sendNotification(Long userId, String title, String message,
                                 NotificationType type,
                                 String referenceType, Long referenceId) {

        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .status(NotificationStatus.PENDING)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();

        notificationRepository.save(notification);

        // For now we just log it. Later we can integrate real Email/SMS.
        log.info("Notification created → userId={}, title={}, type={}", userId, title, type);

        // Simulate sending
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }
}