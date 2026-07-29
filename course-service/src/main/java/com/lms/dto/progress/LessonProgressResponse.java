package com.lms.dto.progress;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Детализированная информация о прогрессе по курсу")
public record LessonProgressResponse(
        @Schema(description = "Общий процент прохождения курса (0-100)", example = "75")
        Integer progressPercent,

        @Schema(description = "Список ID завершенных уроков", example = "[1001, 1002, 1003]")
        List<Long> completedLessonIds,

        @Schema(description = "ID последнего завершенного урока", example = "1003")
        Long lastCompletedLessonId
) {}