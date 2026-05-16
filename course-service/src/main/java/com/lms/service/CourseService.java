package com.lms.service;

import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.CourseRequest;
import com.lms.dto.CourseResponse;
import com.lms.model.Course;
import com.lms.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public List<CourseResponse> getAll() {
        return courseRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CourseResponse getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Курс не найден!"));
        return toResponse(course);
    }

    public CourseResponse create(CourseRequest request) {
        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setPrice(request.price());

        Course saved = courseRepository.save(course);
        return toResponse(saved);
    }

    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Курс не найден!"));

        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setPrice(request.price());

        return toResponse(courseRepository.save(course));
    }

    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Курс с id " + id + " не найден!"));
        courseRepository.delete(course);
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice()
        );
    }
}
