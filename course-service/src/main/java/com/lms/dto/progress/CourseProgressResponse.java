package com.lms.dto.progress;

import java.util.List;

public record CourseProgressResponse(
        Integer progressPercent,
        List<Long> completedLessonIds,
        Long lastCompletedLessonId
) {}
