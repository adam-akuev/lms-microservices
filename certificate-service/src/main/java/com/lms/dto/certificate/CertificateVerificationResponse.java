package com.lms.dto.certificate;

import java.time.LocalDateTime;

public record CertificateVerificationResponse(
        String certificateNumber,
        String studentName,
        String courseTitle,
        Integer score,
        LocalDateTime issuedAt,
        boolean valid
) {}