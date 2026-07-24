package com.lms.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank(message = "Название категории не должно быть пустым")
    @Size(max = 100, message = "Название не может быть длиннее 100 символов")
    String name
) {}