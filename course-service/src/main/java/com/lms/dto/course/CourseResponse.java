package com.lms.dto.course;

import java.math.BigDecimal;

public record CourseResponse(
        Long id,
        String title,
        String description,
        BigDecimal price
) {}
