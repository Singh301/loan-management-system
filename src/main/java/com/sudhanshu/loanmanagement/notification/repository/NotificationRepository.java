package com.sudhanshu.loanmanagement.notification.repository;

import com.sudhanshu.loanmanagement.notification.entity.Notification;
import com.sudhanshu.loanmanagement.notification.entity.Notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);

    List<Notification> findByStatus(NotificationStatus status);
}