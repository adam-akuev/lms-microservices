package com.lms.dto.event;

import java.io.Serializable;

public record EnrollmentCreatedEvent(
        String eventId,
        Long userId,
        String email,
        String fullName,
        Long courseId,
        String courseTitle
) implements Serializable {}