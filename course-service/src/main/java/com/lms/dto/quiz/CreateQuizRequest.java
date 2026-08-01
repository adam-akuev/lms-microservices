package com.lms.dto.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateQuizRequest(

        @NotBlank(message = "Название теста обязательно")
        String title,

        @NotNull
        @Min(1) @Max(100)
        Integer passScore,

        @NotEmpty(message = "Тест должен содержать хотя бы один вопрос")
        @Valid
        List<QuestionRequest> questions
) {
    public record QuestionRequest(

            @NotBlank(message = "Текст вопроса обязателен")
            String text,

            @NotEmpty(message = "У вопроса должно быть минимум 2 варианта ответа")
            @Valid
            List<OptionRequest> options
    ) {}

    public record OptionRequest(

            @NotBlank(message = "Текст варианта ответа обязателен")
            String text,

            @NotNull
            Boolean isCorrect
    ) {}
}