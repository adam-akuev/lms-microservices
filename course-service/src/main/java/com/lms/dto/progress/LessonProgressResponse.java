package com.lms.dto.progress;

import java.util.List;

public record LessonProgressResponse(
        Integer progressPercent,
        List<Long> completedLessonIds,
        Long lastCompletedLessonId
) {}
