package com.lms.service;

import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.progress.CourseProgressResponse;
import com.lms.model.Lesson;
import com.lms.model.LessonProgress;
import com.lms.repository.LessonProgressRepository;
import com.lms.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public int completeLesson(Long studentId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Урок с id " + lessonId + " не найден!"));

        Long courseId = lesson.getCourse().getId();

        LessonProgress progress = lessonProgressRepository.findByStudentIdAndLessonId(studentId, lessonId)
                .orElseGet(() -> {
                    LessonProgress newProgress = new LessonProgress();
                    newProgress.setStudentId(studentId);
                    newProgress.setLessonId(lessonId);
                    return newProgress;
                });

        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            lessonProgressRepository.save(progress);
        }

        return calculateCourseProgress(studentId, courseId);
    }

    public int calculateCourseProgress(Long studentId, Long courseId) {
        long totalLessons = lessonRepository.countByCourseId(courseId);

        if (totalLessons == 0) {
            return 0;
        }

        long completedLessons = lessonProgressRepository.countCompletedLessonsByCourse(studentId, courseId);

        return (int) (completedLessons * 100 / totalLessons);
    }

    public CourseProgressResponse getCourseProgressDetails(Long studentId, Long courseId) {
        Integer progressPercent = calculateCourseProgress(studentId, courseId);
        List<LessonProgress> completedProgress = lessonProgressRepository.findCompletedByStudentAndCourse(studentId, courseId);

        List<Long> completedLessonIds = completedProgress.stream()
                .map(LessonProgress::getLessonId)
                .toList();

        Long lastCompletedLessonId = completedProgress.stream()
                .max(Comparator.comparing(LessonProgress::getId))
                .map(LessonProgress::getLessonId)
                .orElse(null);

        return new CourseProgressResponse(
                progressPercent,
                completedLessonIds,
                lastCompletedLessonId
        );
    }
}
