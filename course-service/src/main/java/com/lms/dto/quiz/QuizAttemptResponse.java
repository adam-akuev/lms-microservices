package com.lms.dto.quiz;

import java.time.LocalDateTime;

public record QuizAttemptResponse(
        Long id,
        Long quizId,
        Integer score,
        Boolean isPassed,
        LocalDateTime createdAt
) {}