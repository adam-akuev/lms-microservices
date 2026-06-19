package com.lms.service;

import com.lms.common.exception.ResourceNotFoundException;
import com.lms.model.Course;
import com.lms.model.Lesson;
import com.lms.model.LessonProgress;
import com.lms.repository.LessonProgressRepository;
import com.lms.repository.LessonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonProgressServiceTest {

    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private LessonRepository lessonRepository;

    @InjectMocks
    private LessonProgressService lessonProgressService;

    // --- Тесты для метода completeLesson ---

    @Test
    void completeLesson_Success_WhenProgressDoesNotExistYet() {
        // Arrange
        Long studentId = 1L;
        Long lessonId = 15L;
        Long courseId = 20L;

        Course course = new Course();
        course.setId(courseId);
        Lesson lesson = Lesson.builder().id(lessonId).course(course).build();

        // Урок существует
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        // Записи о прогрессе в БД еще НЕТ
        when(lessonProgressRepository.findByStudentIdAndLessonId(studentId, lessonId)).thenReturn(Optional.empty());

        // Мокаем данные для последующего расчета прогресса внутри метода
        when(lessonRepository.countByCourseId(courseId)).thenReturn(4L); // всего 4 урока
        when(lessonProgressRepository.countCompletedLessonsByCourse(studentId, courseId)).thenReturn(1L); // 1 пройден

        // Act
        int progressResult = lessonProgressService.completeLesson(studentId, lessonId);

        // Assert
        assertEquals(25, progressResult); // 1 * 100 / 4 = 25%
        // Проверяем, что новый прогресс был сохранен
        verify(lessonProgressRepository, times(1)).save(any(LessonProgress.class));
    }

    @Test
    void completeLesson_Success_WhenAlreadyCompleted() {
        // Arrange
        Long studentId = 1L;
        Long lessonId = 15L;
        Long courseId = 20L;

        Course course = new Course();
        course.setId(courseId);
        Lesson lesson = Lesson.builder().id(lessonId).course(course).build();

        LessonProgress existingProgress = new LessonProgress();
        existingProgress.setStudentId(studentId);
        existingProgress.setLessonId(lessonId);
        existingProgress.setCompleted(true); // Урок УЖЕ был пройден ранее

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonProgressRepository.findByStudentIdAndLessonId(studentId, lessonId)).thenReturn(Optional.of(existingProgress));

        // Мокаем данные для расчета прогресса (например, пройдено 2 из 2)
        when(lessonRepository.countByCourseId(courseId)).thenReturn(2L);
        when(lessonProgressRepository.countCompletedLessonsByCourse(studentId, courseId)).thenReturn(2L);

        // Act
        int progressResult = lessonProgressService.completeLesson(studentId, lessonId);

        // Assert
        assertEquals(100, progressResult); // 2 * 100 / 2 = 100%
        // Важно: так как статус не менялся, метод save() вызываться НЕ должен!
        verify(lessonProgressRepository, never()).save(any(LessonProgress.class));
    }

    @Test
    void completeLesson_ThrowsResourceNotFoundException_WhenLessonNotFound() {
        // Arrange
        Long studentId = 1L;
        Long lessonId = 999L;

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            lessonProgressService.completeLesson(studentId, lessonId);
        });

        verifyNoInteractions(lessonProgressRepository);
    }

    // --- Тесты для метода calculateCourseProgress ---

    @Test
    void calculateCourseProgress_ReturnZero_WhenNoLessonsInCourse() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 30L;

        // В курсе 0 уроков
        when(lessonRepository.countByCourseId(courseId)).thenReturn(0L);

        // Act
        int progress = lessonProgressService.calculateCourseProgress(studentId, courseId);

        // Assert
        assertEquals(0, progress);
        // Запрос на количество пройденных уроков не должен выполняться ради экономии ресурсов
        verify(lessonProgressRepository, never()).countCompletedLessonsByCourse(any(), any());
    }

    @Test
    void calculateCourseProgress_CorrectPercentageCalculation() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 30L;

        when(lessonRepository.countByCourseId(courseId)).thenReturn(3L); // Всего 3 урока
        when(lessonProgressRepository.countCompletedLessonsByCourse(studentId, courseId)).thenReturn(2L); // Пройдено 2

        // Act
        int progress = lessonProgressService.calculateCourseProgress(studentId, courseId);

        // Assert
        assertEquals(66, progress); // 2 * 100 / 3 = 66.66% -> кастуется к int как 66
    }
}