package com.lms.service;

import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.progress.LessonProgressResponse;
import com.lms.mapper.LessonProgressMapper;
import com.lms.model.Lesson;
import com.lms.model.LessonProgress;
import com.lms.repository.LessonProgressRepository;
import com.lms.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressMapper courseProgressMapper;

    @Transactional
    public int completeLesson(Long studentId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Урок с id " + lessonId + " не найден!"));

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

    public LessonProgressResponse getCourseProgressDetails(Long studentId, Long courseId) {
        Integer progressPercent = calculateCourseProgress(studentId, courseId);
        List<LessonProgress> completedProgress = lessonProgressRepository.findCompletedByStudentAndCourse(studentId, courseId);

        return courseProgressMapper.toResponse(progressPercent, completedProgress);
    }
}
