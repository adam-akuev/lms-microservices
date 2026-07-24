package com.lms.dto.course;

import com.lms.dto.category.CategoryResponse;
import com.lms.dto.internal.teacher.TeacherResponseInternal;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Информация о курсе")
public record CourseResponse(
        @Schema(description = "Уникальный идентификатор курса", example = "101")
        Long id,

        @Schema(description = "Название курса", example = "Java Разработчик")
        String title,

        @Schema(description = "Описание курса", example = "Полный курс по разработке бэкенда на Java и Spring Boot")
        String description,

        @Schema(description = "Стоимость курса", example = "19900.00")
        BigDecimal price,

        @Schema(description = "Информация о преподавателе")
        TeacherResponseInternal teacher,

        @Schema(description = "Категория курса")
        CategoryResponse category,

        @Schema(description = "Процент прохождения курса пользователем", example = "45")
        Integer progressPercentage
) {}