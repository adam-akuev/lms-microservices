package com.lms.dto.event;

import java.io.Serializable;

public record TeacherRegistrationEvent(
        Long id,
        String fullName,
        String phone
) implements Serializable {}
