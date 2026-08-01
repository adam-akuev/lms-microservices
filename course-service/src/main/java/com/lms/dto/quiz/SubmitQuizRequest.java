package com.lms.dto.quiz;

import java.util.List;

public record SubmitQuizRequest(
        List<AnswerDto> answers
) {
    public record AnswerDto(
            Long questionId,
            Long selectedOptionId
    ) {}
}