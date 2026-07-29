package com.lms.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Запрос на создание или обновление профиля студента")
public record StudentRequest(
        @Schema(description = "Полное имя студента", example = "Иван Иванов")
        String fullName,

        @Schema(description = "Номер телефона", example = "+79991234567")
        String phone,

        @Schema(description = "Дата рождения", example = "2000-01-15")
        LocalDate birthDate
) {}