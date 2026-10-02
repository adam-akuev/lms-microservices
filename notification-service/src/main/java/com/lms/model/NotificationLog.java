package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}