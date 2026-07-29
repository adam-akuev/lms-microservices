package com.lms.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Запрос на создание или обновление курса")
public record CourseRequest(
        @NotBlank(message = "Название обязательно")
        @Schema(description = "Название курса", example = "Java Разработчик", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = "Подробное описание курса", example = "Полный курс по разработке бэкенда на Java и Spring Boot")
        String description,

        @Positive(message = "Цена должна быть положительной")
        @Schema(description = "Стоимость курса", example = "19900.00")
        BigDecimal price,

        @Schema(description = "Идентификатор преподавателя", example = "5")
        Long teacherId,

        @Schema(description = "Идентификатор категории", example = "1")
        Long categoryId
) {}