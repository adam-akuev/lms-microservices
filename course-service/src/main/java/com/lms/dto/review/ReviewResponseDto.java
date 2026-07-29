package com.lms.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Информация об отзыве")
public record ReviewResponseDto(
        @Schema(description = "Уникальный идентификатор отзыва", example = "50")
        Long id,

        @Schema(description = "Идентификатор студента, оставившего отзыв", example = "1")
        Long studentId,

        @Schema(description = "Оценка курса (1-5)", example = "5")
        int rating,

        @Schema(description = "Текст отзыва", example = "Отличный курс, всё структурировано и понятно!")
        String text,

        @Schema(description = "Дата и время создания отзыва", example = "2026-07-24T18:00:00")
        LocalDateTime createdAt
) {}