package com.lms.dto.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public record QuizPassedEvent(
        Long userId,
        Long courseId,
        Integer score,
        LocalDateTime passedAt
) implements Serializable {}