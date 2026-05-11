package com.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CourseRequest(

        @NotBlank(message = "Название обязательно")
        String title,

        String description,

        @Positive(message = "Цена должна быть положительной")
        BigDecimal price
) {}
