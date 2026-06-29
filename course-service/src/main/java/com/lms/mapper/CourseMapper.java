package com.lms.mapper;

import com.lms.dto.course.CourseRequest;
import com.lms.dto.course.CourseResponse;
import com.lms.dto.internal.teacher.TeacherResponseInternal;
import com.lms.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "id", source = "course.id")
    @Mapping(target = "title", source = "course.title")
    @Mapping(target = "description", source = "course.description")
    @Mapping(target = "price", source = "course.price")
    @Mapping(target = "teacher", source = "teacher")
    @Mapping(target = "progressPercentage", source = "progressPercentage")
    CourseResponse toResponse(Course course, TeacherResponseInternal teacher, Integer progressPercentage);

    default CourseResponse toResponse(Course course, TeacherResponseInternal teacher) {
        return toResponse(course, teacher, null);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "teacherId", source = "targetTeacherId")
    Course toEntity(CourseRequest request, Long targetTeacherId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "teacherId", ignore = true)
    void updateEntityFromDto(CourseRequest request, @MappingTarget Course course);
}
