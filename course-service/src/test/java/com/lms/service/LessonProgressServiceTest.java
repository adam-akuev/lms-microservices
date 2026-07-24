package com.lms.service;

import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.progress.LessonProgressResponse;
import com.lms.mapper.LessonProgressMapper;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonProgressServiceTest {

    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private LessonProgressMapper courseProgressMapper;

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

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonProgressRepository.findByStudentIdAndLessonId(studentId, lessonId)).thenReturn(Optional.empty());
        when(lessonRepository.countByCourseId(courseId)).thenReturn(4L);
        when(lessonProgressRepository.countCompletedLessonsByCourse(studentId, courseId)).thenReturn(1L);

        // Act
        int progressResult = lessonProgressService.completeLesson(studentId, lessonId);

        // Assert
        assertEquals(25, progressResult);
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
        existingProgress.setCompleted(true);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonProgressRepository.findByStudentIdAndLessonId(studentId, lessonId)).thenReturn(Optional.of(existingProgress));
        when(lessonRepository.countByCourseId(courseId)).thenReturn(2L);
        when(lessonProgressRepository.countCompletedLessonsByCourse(studentId, courseId)).thenReturn(2L);

        // Act
        int progressResult = lessonProgressService.completeLesson(studentId, lessonId);

        // Assert
        assertEquals(100, progressResult);
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

        when(lessonRepository.countByCourseId(courseId)).thenReturn(0L);

        // Act
        int progress = lessonProgressService.calculateCourseProgress(studentId, courseId);

        // Assert
        assertEquals(0, progress);
        verify(lessonProgressRepository, never()).countCompletedLessonsByCourse(anyLong(), anyLong());
    }

    @Test
    void calculateCourseProgress_CorrectPercentageCalculation() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 30L;

        when(lessonRepository.countByCourseId(courseId)).thenReturn(3L);
        when(lessonProgressRepository.countCompletedLessonsByCourse(studentId, courseId)).thenReturn(2L);

        // Act
        int progress = lessonProgressService.calculateCourseProgress(studentId, courseId);

        // Assert
        assertEquals(66, progress);
    }

    // --- Тесты для метода getCourseProgressDetails ---

    @Test
    void getCourseProgressDetails_Success() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 30L;

        // Мокаем расчет прогресса
        when(lessonRepository.countByCourseId(courseId)).thenReturn(3L);
        when(lessonProgressRepository.countCompletedLessonsByCourse(studentId, courseId)).thenReturn(2L);

        // Создаем список завершенных уроков (реальные объекты LessonProgress)
        LessonProgress progress1 = new LessonProgress();
        progress1.setId(1L);
        progress1.setStudentId(studentId);
        progress1.setLessonId(10L);
        progress1.setCompleted(true);
        progress1.setCompletedAt(LocalDateTime.now());

        LessonProgress progress2 = new LessonProgress();
        progress2.setId(2L);
        progress2.setStudentId(studentId);
        progress2.setLessonId(20L);
        progress2.setCompleted(true);
        progress2.setCompletedAt(LocalDateTime.now());

        List<LessonProgress> completedProgress = List.of(progress1, progress2);
        when(lessonProgressRepository.findCompletedByStudentAndCourse(studentId, courseId))
                .thenReturn(completedProgress);

        // Создаем ожидаемый ответ с List<Long> (ID уроков), а не List<LessonProgress>
        List<Long> completedLessonIds = List.of(10L, 20L);
        LessonProgressResponse expectedResponse = new LessonProgressResponse(66, completedLessonIds, 20L);
        when(courseProgressMapper.toResponse(66, completedProgress)).thenReturn(expectedResponse);

        // Act
        LessonProgressResponse response = lessonProgressService.getCourseProgressDetails(studentId, courseId);

        // Assert
        assertNotNull(response);
        assertEquals(66, response.progressPercent());
        assertEquals(completedLessonIds, response.completedLessonIds());
        assertEquals(20L, response.lastCompletedLessonId());
    }

    @Test
    void getCourseProgressDetails_ReturnZero_WhenNoLessons() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 30L;

        when(lessonRepository.countByCourseId(courseId)).thenReturn(0L);

        // Пустой список ID уроков
        List<Long> emptyLessonIds = Collections.emptyList();
        LessonProgressResponse expectedResponse = new LessonProgressResponse(0, emptyLessonIds, null);
        when(courseProgressMapper.toResponse(0, Collections.emptyList())).thenReturn(expectedResponse);

        // Act
        LessonProgressResponse response = lessonProgressService.getCourseProgressDetails(studentId, courseId);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.progressPercent());
        assertTrue(response.completedLessonIds().isEmpty());
        assertNull(response.lastCompletedLessonId());
    }
}