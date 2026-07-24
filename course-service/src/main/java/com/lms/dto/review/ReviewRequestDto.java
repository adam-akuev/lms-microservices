package com.lms.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на создание отзыва к курсу")
public record ReviewRequestDto(
        @Min(1) @Max(5)
        @Schema(description = "Оценка курса по шкале от 1 до 5", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        int rating,

        @NotBlank
        @Schema(description = "Текст отзыва", example = "Отличный курс, всё структурировано и понятно!", requiredMode = Schema.RequiredMode.REQUIRED)
        String text
) {}