package com.lms.mapper;

import com.lms.dto.lesson.LessonRequest;
import com.lms.dto.lesson.LessonResponse;
import com.lms.model.Course;
import com.lms.model.Lesson;
import jakarta.persistence.EntityManager;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class LessonMapper {

    private final EntityManager entityManager;

    protected LessonMapper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public abstract LessonResponse toResponse(Lesson lesson);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", source = "courseId")
    public abstract Lesson toEntity(LessonRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", source = "courseId")
    public abstract void updateEntityFromDto(LessonRequest request, @MappingTarget Lesson lesson);

    Course mapCourseIdToCourse(Long courseId) {
        return entityManager.getReference(Course.class, courseId);
    }
}
