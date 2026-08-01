package com.lms.dto.quiz;

import java.util.List;

public record StudentQuizDto(
        Long id,
        String title,
        List<StudentQuestionDto> questions
) {
    public record StudentQuestionDto(
            Long id,
            String text,
            List<StudentOptionDto> options
    ) {}

    public record StudentOptionDto(
            Long id,
            String text
    ) {}
}
