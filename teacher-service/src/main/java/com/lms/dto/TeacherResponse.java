package com.lms.dto;

import com.lms.model.Teacher;

import java.time.LocalDate;

public record TeacherResponse(
        Long id,
        String fullName,
        String phone,
        LocalDate birthDate,
        String bio,
        String qualification,
        Integer experienceYears
) {
    public static TeacherResponse fromEntity(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getFullName(),
                teacher.getPhone(),
                teacher.getBirthDate(),
                teacher.getBio(),
                teacher.getQualification(),
                teacher.getExperienceYears()
        );
    }
}
