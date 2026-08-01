package com.lms.dto.quiz;

import java.util.List;

public record QuizAdminDto(
        Long id,
        String title,
        Integer passScore,
        List<QuestionDto> questions
) {
    public record QuestionDto(
            Long id,
            String text,
            List<OptionDto> options
    ) {}

    public record OptionDto(
            Long id,
            String text,
            Boolean isCorrect
    ) {}
}