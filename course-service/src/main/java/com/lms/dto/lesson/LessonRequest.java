package com.lms.dto.lesson;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание или обновление урока")
public record LessonRequest(
        @NotBlank(message = "Название урока не может быть пустым")
        @Size(min = 3, max = 150, message = "Название урока должно быть от 3 до 150 символов")
        @Schema(description = "Название урока", example = "Введение в Spring Security", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @NotBlank(message = "Контент урока не может быть пустым")
        @Schema(description = "Содержимое/текст урока", example = "В этом уроке мы разберем базовые принципы аутентификации...", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,

        @NotNull(message = "ID курса должен быть обязательно указан")
        @Schema(description = "Идентификатор курса", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
        Long courseId
) {}