package com.lms.dto.internal;

import com.lms.model.Teacher;

public record TeacherResponseInternal(
        Long id,
        String fullName,
        String qualification,
        Integer experienceYears
) {
    public static TeacherResponseInternal fromEntity(Teacher teacher) {
        return new TeacherResponseInternal(
                teacher.getId(),
                teacher.getFullName(),
                teacher.getQualification(),
                teacher.getExperienceYears()
        );
    }
}