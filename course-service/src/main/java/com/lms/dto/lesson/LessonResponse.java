package com.lms.dto.lesson;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация об уроке")
public record LessonResponse(
        @Schema(description = "Уникальный идентификатор урока", example = "1001")
        Long id,

        @Schema(description = "Название урока", example = "Введение в Spring Security")
        String title,

        @Schema(description = "Содержимое/текст урока", example = "В этом уроке мы разберем базовые принципы аутентификации...")
        String content
) {}