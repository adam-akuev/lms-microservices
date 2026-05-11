package com.lms.dto;

import com.lms.model.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
        String password,

        @NotBlank(message = "ФИО обязательно для заполнения")
        @Pattern(
                regexp = "^[а-яА-ЯёЁa-zA-Z\\s\\-]+$",
                message = "ФИО может содержать только буквы, пробелы и дефис"
        )
        String fullName,

        @Pattern(
                regexp = "^(\\+7|7|8)?[\\s\\-]?\\(?[489][0-9]{2}\\)?[\\s\\-]?[0-9]{3}[\\s\\-]?[0-9]{2}[\\s\\-]?[0-9]{2}$",
                message = "Неверный формат номера телефона"
        )
        String phoneNumber,

        @NotNull(message = "Нужно выбрать роль (Студент/Учитель)")
        Role role
) {}
