package com.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на аутентификацию")
public record LoginRequest(
        @Schema(description = "Электронная почта пользователя", example = "student@javarush.ru")
        @Email
        @NotBlank
        String email,

        @Schema(description = "Пароль пользователя", example = "SecretPass123!")
        @NotBlank
        String password
) {}
