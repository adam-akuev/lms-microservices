package com.lms.service;

import com.lms.client.EnrollmentClient;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.lesson.LessonRequest;
import com.lms.dto.lesson.LessonResponse;
import com.lms.mapper.LessonMapper;
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
    private final EnrollmentClient enrollmentClient;
    private final LessonMapper lessonMapper;

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

        return lessonsCourse.stream().map(lessonMapper::toResponse).toList();
    }

    public LessonResponse getById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Урок с ID " + id + " не найден"));

        return lessonMapper.toResponse(lesson);
    }

    @Transactional
    public LessonResponse create(LessonRequest request) {
        Lesson lesson = lessonMapper.toEntity(request);
        Lesson savedLesson = lessonRepository.save(lesson);
        return lessonMapper.toResponse(savedLesson);
    }

    @Transactional
    public LessonResponse update(Long id, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Урок с ID " + id + " не найден"));

        lessonMapper.updateEntityFromDto(request, lesson);

        Lesson updatedLesson = lessonRepository.save(lesson);
        return lessonMapper.toResponse(updatedLesson);
    }

    @Transactional
    public void delete(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Урок с ID " + id + " не найден"));

        lessonRepository.delete(lesson);
    }
}
