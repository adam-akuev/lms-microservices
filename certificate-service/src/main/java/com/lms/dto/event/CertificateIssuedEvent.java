package com.lms.dto.event;

import java.io.Serializable;

public record CertificateIssuedEvent(
        String eventId,
        Long userId,
        String email,
        String studentName,
        String courseTitle,
        String certificateNumber,
        Integer score
) implements Serializable {}