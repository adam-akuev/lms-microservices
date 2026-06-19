package com.lms.dto.internal.teacher;

public record TeacherResponseInternal(
        Long id,
        String fullName,
        String qualification,
        Integer experienceYears
) {}
