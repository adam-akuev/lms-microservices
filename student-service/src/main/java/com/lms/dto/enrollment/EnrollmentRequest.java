package com.lms.dto.enrollment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на запись студента на курс")
public record EnrollmentRequest(
        @NotNull(message = "ID курса не должен быть пустым")
        @Schema(description = "Идентификатор курса", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
        Long courseId
) {}