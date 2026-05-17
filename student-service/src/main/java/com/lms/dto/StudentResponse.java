package com.lms.dto;

import com.lms.model.Student;

import java.time.LocalDate;

public record StudentResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        LocalDate birthDate
) {
    public static StudentResponse fromEntity(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getPhone(),
                student.getBirthDate()
        );
    }
}
