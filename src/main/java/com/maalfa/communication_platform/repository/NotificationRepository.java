package com.maalfa.communication_platform.repository;

import com.maalfa.communication_platform.entity.Notification;
import com.maalfa.communication_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByRecipientAndReadFalseOrderByCreatedAtDesc(User recipient);

    List<Notification> findAllByRecipientOrderByCreatedAtDesc(User recipient);

    long countByRecipientAndReadFalse(User recipient);
}
