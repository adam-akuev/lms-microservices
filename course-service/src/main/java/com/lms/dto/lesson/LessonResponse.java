package com.lms.dto.lesson;

public record LessonResponse(
        Long id,
        String title,
        String content
) {}
