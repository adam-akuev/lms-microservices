package com.lms.dto.quiz;

public record QuizResponse(
        Long id,
        Long courseId,
        String title,
        Integer passScore,
        int questionsCount
) {}