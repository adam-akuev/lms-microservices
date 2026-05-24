package com.lms.dto.enrollment;

import com.lms.model.Enrollment;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        Long courseId,
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
