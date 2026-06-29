package com.lms.service;

import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.review.ReviewRequestDto;
import com.lms.dto.review.ReviewResponseDto;
import com.lms.mapper.ReviewMapper;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private Course sampleCourse;
    private Review sampleReview;
    private ReviewRequestDto reviewRequestDto;
    private ReviewResponseDto sampleResponseDto;

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

        sampleResponseDto = new ReviewResponseDto(
                100L, 42L, 5, "Отличный курс!", sampleReview.getCreatedAt()
        );
    }

    @Test
    void findAll_ShouldReturnPagedReviews() {
        // Given
        Long courseId = 1L;
        Pageable pageable = PageRequest.of(0, 5);
        Page<Review> reviewPage = new PageImpl<>(List.of(sampleReview));

        when(reviewRepository.findByCourseId(courseId, pageable)).thenReturn(reviewPage);
        when(reviewMapper.toResponseDto(sampleReview)).thenReturn(sampleResponseDto);

        // When
        Page<ReviewResponseDto> result = reviewService.findAll(courseId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Отличный курс!", result.getContent().get(0).text());
        assertEquals(5, result.getContent().get(0).rating());
        assertEquals(42L, result.getContent().get(0).studentId());

        verify(reviewRepository, times(1)).findByCourseId(courseId, pageable);
        verify(reviewMapper, times(1)).toResponseDto(sampleReview);
    }

    @Test
    void addReview_Success_ShouldSaveReviewAndUpdateCourseStats() {
        // Given
        Long studentId = 42L;
        Long courseId = 1L;

        when(reviewRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.empty());

        when(reviewMapper.toEntity(reviewRequestDto, studentId, courseId)).thenReturn(sampleReview);
        when(reviewMapper.toResponseDto(sampleReview)).thenReturn(sampleResponseDto);

        when(reviewRepository.save(any(Review.class))).thenReturn(sampleReview);

        ReviewStats mockStats = mock(ReviewStats.class);
        when(mockStats.getAvgRating()).thenReturn(4.5);
        when(mockStats.getReviewCount()).thenReturn(10L);
        when(reviewRepository.getRatingStatsByCourseId(courseId)).thenReturn(mockStats);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(sampleCourse));

        // When
        ReviewResponseDto response = reviewService.addReview(studentId, courseId, reviewRequestDto);

        // Then
        assertNotNull(response);
        assertEquals(5, response.rating());
        assertEquals("Отличный курс!", response.text());
        assertEquals(42L, response.studentId());
        assertEquals(100L, response.id());

        assertEquals(4.5, sampleCourse.getAverageRating());
        assertEquals(10, sampleCourse.getReviewCount());

        verify(reviewMapper).toEntity(reviewRequestDto, studentId, courseId);
        verify(reviewRepository).save(sampleReview);
        verify(reviewMapper).toResponseDto(sampleReview);
        verify(courseRepository).save(sampleCourse);
    }

    @Test
    void addReview_AlreadyExists_ShouldThrowIllegalArgumentException() {
        // Given
        Long studentId = 42L;
        Long courseId = 1L;

        when(reviewRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.of(sampleReview));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reviewService.addReview(studentId, courseId, reviewRequestDto)
        );

        assertEquals("Вы уже оставили отзыв к этому курсу", exception.getMessage());

        verifyNoInteractions(reviewMapper);
        verify(reviewRepository, never()).save(any(Review.class));
        verify(courseRepository, never()).findById(anyLong());
        verify(reviewRepository, never()).getRatingStatsByCourseId(anyLong());
    }

    @Test
    void addReview_CourseNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        Long studentId = 42L;
        Long courseId = 999L;

        when(reviewRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.empty());

        when(reviewMapper.toEntity(reviewRequestDto, studentId, courseId)).thenReturn(sampleReview);

        when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);

        // Статистика нужна, но getAvgRating и getReviewCount НЕ будут вызваны
        ReviewStats mockStats = mock(ReviewStats.class);
        // Используем lenient() так как эти стаббинги не будут использованы
        // из-за исключения в courseRepository.findById()
        lenient().when(mockStats.getAvgRating()).thenReturn(4.5);
        lenient().when(mockStats.getReviewCount()).thenReturn(10L);
        when(reviewRepository.getRatingStatsByCourseId(courseId)).thenReturn(mockStats);

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                reviewService.addReview(studentId, courseId, reviewRequestDto)
        );

        assertEquals("Курс не найден!", exception.getMessage());

        verify(reviewMapper).toEntity(reviewRequestDto, studentId, courseId);
        verify(reviewRepository).save(sampleReview);
        verify(reviewRepository).getRatingStatsByCourseId(courseId);
        verify(courseRepository).findById(courseId);
        verify(courseRepository, never()).save(any(Course.class));
        verify(reviewMapper, never()).toResponseDto(any(Review.class));
    }
}