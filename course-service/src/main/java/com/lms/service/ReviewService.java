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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final ReviewMapper reviewMapper;

    public Page<ReviewResponseDto> findAll(Long courseId, Pageable pageable) {
        return reviewRepository.findByCourseId(courseId, pageable).map(reviewMapper::toResponseDto);
    }

    @Transactional
    public ReviewResponseDto addReview(Long studentId, Long courseId, ReviewRequestDto reviewDto) {
        if (reviewRepository.findByStudentIdAndCourseId(studentId, courseId).isPresent()) {
            throw new IllegalArgumentException("Вы уже оставили отзыв к этому курсу");
        }

        Review review = reviewMapper.toEntity(reviewDto, studentId, courseId);
        reviewRepository.save(review);

        ReviewStats ratingStats = reviewRepository.getRatingStatsByCourseId(courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Курс не найден!"));

        Double avgRating = ratingStats.getAvgRating();
        Long reviewCount = ratingStats.getReviewCount();

        course.setAverageRating(avgRating != null ? avgRating : 0.0);
        course.setReviewCount(reviewCount != null ? reviewCount.intValue() : 0);
        courseRepository.save(course);

        return reviewMapper.toResponseDto(review);
    }
}
