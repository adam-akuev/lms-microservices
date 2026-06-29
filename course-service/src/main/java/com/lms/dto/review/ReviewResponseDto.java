package com.lms.dto.review;

import com.lms.model.Review;

import java.time.LocalDateTime;

public record ReviewResponseDto(
        Long id,
        Long studentId,
        int rating,
        String text,
        LocalDateTime createdAt
) {}
