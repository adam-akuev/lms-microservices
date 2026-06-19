package com.lms.service;

import com.lms.client.EnrollmentClient;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.lesson.LessonRequest;
import com.lms.dto.lesson.LessonResponse;
import com.lms.model.Course;
import com.lms.model.Lesson;
import com.lms.repository.CourseRepository;
import com.lms.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentClient enrollmentClient;

    public List<LessonResponse> getAllLessonsForStudentByCourseId(Long studentId, Long courseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isTeacherOrAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER") ||
                                a.getAuthority().equals("ROLE_ADMIN"));

        if (isTeacherOrAdmin) {
            return getAllLessonsByCourseId(courseId);
        }

        boolean isEnrolled = enrollmentClient.checkEnrollment(studentId, courseId);

        if (!isEnrolled) {
            throw new AccessDeniedException("У вас нет доступа к урокам этого курса. Сначала запишитесь!");
        }

        return getAllLessonsByCourseId(courseId);
    }

    private List<LessonResponse> getAllLessonsByCourseId(Long courseId) {
        List<Lesson> lessonsCourse = lessonRepository.findByCourseId(courseId);

        return lessonsCourse.stream().map(LessonResponse::fromEntity).toList();
    }

    public LessonResponse getById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Урок с ID " + id + " не найден"));

        return LessonResponse.fromEntity(lesson);
    }

    @Transactional
    public LessonResponse create(LessonRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Курс с ID " + request.courseId() + " не найден"));

        Lesson lesson = Lesson.builder()
                .title(request.title())
                .content(request.content())
                .course(course)
                .build();

        Lesson savedLesson = lessonRepository.save(lesson);
        return LessonResponse.fromEntity(savedLesson);
    }

    @Transactional
    public LessonResponse update(Long id, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Урок с ID " + id + " не найден"));

        Course courseRequest = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Курс с ID " + request.courseId() + " не найден"));

        lesson.setTitle(request.title());
        lesson.setContent(request.content());
        lesson.setCourse(courseRequest);

        Lesson updatedLesson = lessonRepository.save(lesson);
        return LessonResponse.fromEntity(updatedLesson);
    }

    @Transactional
    public void delete(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Урок с ID " + id + " не найден"));

        lessonRepository.delete(lesson);
    }
}
