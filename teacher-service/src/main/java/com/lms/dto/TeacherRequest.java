package com.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Запрос на создание или обновление профиля преподавателя")
public record TeacherRequest(
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
) {}