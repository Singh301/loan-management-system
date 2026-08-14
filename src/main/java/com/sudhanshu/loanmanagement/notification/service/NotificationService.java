package com.sudhanshu.loanmanagement.notification.service;

import com.sudhanshu.loanmanagement.notification.entity.Notification;

import java.util.List;

public interface NotificationService {

    void sendNotification(Long userId, String title, String message,
                          Notification.NotificationType type,
                          String referenceType, Long referenceId);

    List<Notification> getUserNotifications(Long userId);

    void markAsRead(Long notificationId);
}