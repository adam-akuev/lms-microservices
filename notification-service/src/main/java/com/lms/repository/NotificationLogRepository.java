package com.lms.repository;

import com.lms.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Optional<NotificationLog> findByEventId(String eventId);
    boolean existsByEventIdAndStatus(String eventId, com.lms.model.NotificationStatus status);
}