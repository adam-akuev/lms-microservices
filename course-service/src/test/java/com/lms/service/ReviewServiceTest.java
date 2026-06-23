package com.lms.service;

import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.review.ReviewRequestDto;
import com.lms.dto.review.ReviewResponseDto;
import com.lms.model.Course;
import com.lms.model.Review;
import com.lms.repository.CourseRepository;
import com.lms.repository.ReviewRepository;
import com.lms.repository.ReviewStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Course sampleCourse;
    private Review sampleReview;
    private ReviewRequestDto reviewRequestDto;

    @BeforeEach
    void setUp() {
        sampleCourse = new Course();
        sampleCourse.setId(1L);
        sampleCourse.setTitle("Java Backend Developer");
        sampleCourse.setAverageRating(0.0);
        sampleCourse.setReviewCount(0);

        sampleReview = new Review();
        sampleReview.setId(100L);
        sampleReview.setStudentId(42L);
        sampleReview.setCourseId(1L);
        sampleReview.setRating(5);
        sampleReview.setText("Отличный курс!");
        sampleReview.setCreatedAt(LocalDateTime.now());

        reviewRequestDto = new ReviewRequestDto(5, "Отличный курс!");
    }

    @Test
    void findAll_ShouldReturnPagedReviews() {
        // Given
        Long courseId = 1L;
        Pageable pageable = PageRequest.of(0, 5);
        Page<Review> reviewPage = new PageImpl<>(List.of(sampleReview));

        when(reviewRepository.findByCourseId(courseId, pageable)).thenReturn(reviewPage);

        // When
        Page<ReviewResponseDto> result = reviewService.findAll(courseId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Отличный курс!", result.getContent().get(0).text());
        assertEquals(5, result.getContent().get(0).rating());
        
        verify(reviewRepository, times(1)).findByCourseId(courseId, pageable);
    }

    @Test
    void addReview_Success_ShouldSaveReviewAndUpdateCourseStats() {
        // Given
        Long studentId = 42L;
        Long courseId = 1L;

        // Настраиваем поведение: отзыва от этого студента еще нет
        when(reviewRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(sampleReview);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(sampleCourse));

        // Мокаем интерфейс-проекцию ReviewStats
        ReviewStats mockStats = mock(ReviewStats.class);
        when(mockStats.getAvgRating()).thenReturn(4.5);
        when(mockStats.getReviewCount()).thenReturn(10L);
        when(reviewRepository.getRatingStatsByCourseId(courseId)).thenReturn(mockStats);

        // When
        ReviewResponseDto response = reviewService.addReview(studentId, courseId, reviewRequestDto);

        // Then
        assertNotNull(response);
        assertEquals(5, response.rating());
        assertEquals("Отличный курс!", response.text());
        assertEquals(studentId, response.studentId());

        // Проверяем, что статистика в сущности курса обновилась
        assertEquals(4.5, sampleCourse.getAverageRating());
        assertEquals(10, sampleCourse.getReviewCount());

        // Проверяем вызовы репозиториев
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(courseRepository, times(1)).save(sampleCourse);
    }

    @Test
    void addReview_AlreadyExists_ShouldThrowIllegalArgumentException() {
        // Given
        Long studentId = 42L;
        Long courseId = 1L;

        // Имитируем, что отзыв уже существует
        when(reviewRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.of(sampleReview));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            reviewService.addReview(studentId, courseId, reviewRequestDto)
        );

        assertEquals("Вы уже оставили отзыв к этому курсу", exception.getMessage());
        
        // Проверяем, что сохранение и обновление курса не вызывались
        verify(reviewRepository, never()).save(any(Review.class));
        verify(courseRepository, never()).findById(anyLong());
    }

    @Test
    void addReview_CourseNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        Long studentId = 42L;
        Long courseId = 999L; // Несуществующий ID

        when(reviewRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.empty());
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        ReviewStats mockStats = mock(ReviewStats.class);
        when(reviewRepository.getRatingStatsByCourseId(courseId)).thenReturn(mockStats);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> 
            reviewService.addReview(studentId, courseId, reviewRequestDto)
        );

        assertEquals("Курс не найден!", exception.getMessage());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(courseRepository, never()).save(any(Course.class));
    }
}