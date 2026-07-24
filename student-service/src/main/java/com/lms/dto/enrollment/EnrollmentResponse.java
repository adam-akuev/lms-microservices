package com.lms.dto.enrollment;

import com.lms.model.Enrollment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Информация о записи на курс")
public record EnrollmentResponse(
        @Schema(description = "Уникальный идентификатор записи", example = "42")
        Long id,

        @Schema(description = "Идентификатор студента", example = "1")
        Long studentId,

        @Schema(description = "Идентификатор курса", example = "101")
        Long courseId,

        @Schema(description = "Дата и время записи на курс", example = "2026-07-24T14:30:00")
        LocalDateTime enrolledAt
) {
    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getCourseId(),
                enrollment.getEnrolledAt()
        );
    }
}