package com.lms.dto.lesson;

import com.lms.model.Lesson;

public record LessonResponse(
        Long id,
        String title,
        String content
) {
    public static LessonResponse fromEntity(Lesson lesson) {
        return new LessonResponse(lesson.getId(), lesson.getTitle(), lesson.getContent());
    }
}
