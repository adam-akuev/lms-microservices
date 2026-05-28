package com.lms.service;

import com.lms.client.EnrollmentClient;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.course.CourseRequest;
import com.lms.dto.course.CourseResponse;
import com.lms.model.Course;
import com.lms.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentClient enrollmentClient;
    private final RabbitTemplate rabbitTemplate;

    public List<CourseResponse> getAll() {
        return courseRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CourseResponse getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Курс не найден!"));
        return toResponse(course);
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setPrice(request.price());

        Course saved = courseRepository.save(course);
        return toResponse(saved);
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Курс не найден!"));

        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setPrice(request.price());

        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Курс с id " + id + " не найден!"));

        courseRepository.delete(course);

        rabbitTemplate.convertAndSend(
                "lms-exchange",
                "course.deleted.key",
                id
        );
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
