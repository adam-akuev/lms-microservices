package com.lms.dto;

import com.lms.model.Student;

import java.time.LocalDate;

public record StudentResponse(
        Long id,
        String fullName,
        String phone,
        LocalDate birthDate
) {
    public static StudentResponse fromEntity(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getFullName(),
                student.getPhone(),
                student.getBirthDate()
        );
    }
}
