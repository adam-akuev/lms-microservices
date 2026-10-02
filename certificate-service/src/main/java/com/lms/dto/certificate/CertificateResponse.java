package com.lms.dto.certificate;

import java.time.LocalDateTime;

public record CertificateResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String certificateNumber,
        Integer score,
        LocalDateTime issuedAt
) {}