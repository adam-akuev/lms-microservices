package com.lms.dto;

import com.lms.model.Teacher;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Данные профиля преподавателя")
public record TeacherResponse(
        @Schema(description = "Уникальный идентификатор преподавателя", example = "5")
        Long id,

        @Schema(description = "Полное имя преподавателя", example = "Алексей Петров")
        String fullName,

        @Schema(description = "Номер телефона", example = "+79998887766")
        String phone,

        @Schema(description = "Дата рождения", example = "1985-04-12")
        LocalDate birthDate,

        @Schema(description = "Краткая биография и информация о себе", example = "Преподаватель высшей категории, эксперт по Java и Spring Boot")
        String bio,

        @Schema(description = "Квалификация или ученая степень", example = "Кандидат технических наук")
        String qualification,

        @Schema(description = "Опыт работы в годах", example = "10")
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