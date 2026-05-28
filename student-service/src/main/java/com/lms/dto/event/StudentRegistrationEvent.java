package com.lms.dto.event;

import java.io.Serializable;

public record StudentRegistrationEvent(
        Long id,
        String fullName,
        String phone
) implements Serializable {}
