package com.lms.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReviewRequestDto(
        @Min(1) @Max(5)
        int rating,

        @NotBlank
        String text
) {}
