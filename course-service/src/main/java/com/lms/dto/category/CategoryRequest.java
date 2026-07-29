package com.lms.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание или обновление категории")
public record CategoryRequest(
        @NotBlank(message = "Название категории не должно быть пустым")
        @Size(max = 100, message = "Название не может быть длиннее 100 символов")
        @Schema(description = "Название категории", example = "Программирование", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
) {}