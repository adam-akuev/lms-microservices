package com.lms.dto.event;

import java.io.Serializable;

public record UserRegisteredEvent(
        String eventId,
        Long userId,
        String email,
        String fullName
) implements Serializable {}