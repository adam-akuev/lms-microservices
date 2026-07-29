package com.lms.dto.student;

import com.lms.model.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Данные профиля студента")
public record StudentResponse(
        @Schema(description = "Уникальный идентификатор студента", example = "1")
        Long id,

        @Schema(description = "Полное имя студента", example = "Иван Иванов")
        String fullName,

        @Schema(description = "Номер телефона", example = "+79991234567")
        String phone,

        @Schema(description = "Дата рождения", example = "2000-01-15")
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