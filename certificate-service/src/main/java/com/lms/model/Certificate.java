package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_title", nullable = false)
    private String courseTitle;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "certificate_number", nullable = false, unique = true, length = 64)
    private String certificateNumber;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
        if (this.certificateNumber == null) {
            this.certificateNumber = "CERT-" + UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 16)
                    .toUpperCase();
        }
    }
}