package com.lms.dto.course;

import com.lms.dto.internal.teacher.TeacherResponseInternal;

import java.math.BigDecimal;

public record CourseResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        TeacherResponseInternal teacher,
        Integer progressPercentage
) {}
