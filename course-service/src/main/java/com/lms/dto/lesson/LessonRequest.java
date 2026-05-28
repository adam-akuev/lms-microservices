package com.lms.dto.lesson;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonRequest(

        @NotBlank(message = "Название урока не может быть пустым")
        @Size(min = 3, max = 150, message = "Название урока должно быть от 3 до 150 символов")
        String title,

        @NotBlank(message = "Контент урока не может быть пустым")
        String content,

        @NotNull(message = "ID курса должен быть обязательно указан")
        Long courseId
) {}
