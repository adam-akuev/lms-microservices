package com.lms.dto;

import com.lms.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Запрос на регистрацию нового пользователя")
public record RegisterRequest(
        @Schema(
                description = "Электронная почта пользователя",
                example = "ivan.ivanov@example.com"
        )
        @Email
        @NotBlank
        String email,

        @Schema(
                description = "Пароль пользователя (не менее 6 символов)",
                example = "SecretPass123!",
                minLength = 6
        )
        @NotBlank
        @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
        String password,

        @Schema(
                description = "ФИО пользователя (только буквы, пробелы и дефис)",
                example = "Иванов Иван Иванович"
        )
        @NotBlank(message = "ФИО обязательно для заполнения")
        @Pattern(
                regexp = "^[а-яА-ЯёЁa-zA-Z\\s\\-]+$",
                message = "ФИО может содержать только буквы, пробелы и дефис"
        )
        String fullName,

        @Schema(
                description = "Номер телефона в российском формате",
                example = "+79991234567"
        )
        @Pattern(
                regexp = "^(\\+7|7|8)?[\\s\\-]?\\(?[489][0-9]{2}\\)?[\\s\\-]?[0-9]{3}[\\s\\-]?[0-9]{2}[\\s\\-]?[0-9]{2}$",
                message = "Неверный формат номера телефона"
        )
        String phoneNumber,

        @Schema(
                description = "Роль пользователя в системе (STUDENT / TEACHER)",
                example = "STUDENT"
        )
        @NotNull(message = "Нужно выбрать роль (Студент/Учитель)")
        Role role
) {}