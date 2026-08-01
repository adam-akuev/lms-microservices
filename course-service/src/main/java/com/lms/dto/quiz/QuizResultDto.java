package com.lms.dto.quiz;

public record QuizResultDto(
        Long quizId,
        Integer score,
        Boolean isPassed,
        String message
) {}