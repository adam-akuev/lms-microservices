package com.lms.mapper;

import com.lms.dto.progress.LessonProgressResponse;
import com.lms.model.LessonProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonProgressMapper {

    @Mapping(target = "lastCompletedLessonId", expression = "java(findLastCompletedId(completedProgress))")
    LessonProgressResponse toResponse(Integer progressPercent, List<LessonProgress> completedProgress);

    default Long mapLessonProgressToLong(LessonProgress progress) {
        return progress != null ? progress.getLessonId() : null;
    }

    default Long findLastCompletedId(List<LessonProgress> progressList) {
        if (progressList == null || progressList.isEmpty()) {
            return null;
        }

        return progressList.stream()
                .max(Comparator.comparing(LessonProgress::getId))
                .map(LessonProgress::getLessonId)
                .orElse(null);
    }
}
