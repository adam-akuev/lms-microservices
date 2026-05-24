package com.lms.dto.enrollment;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
        @NotNull(message = "ID курса не должен быть пустым")
        Long courseId
) {}
