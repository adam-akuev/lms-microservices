package com.lms.controller;

import com.lms.dto.review.ReviewRequestDto;
import com.lms.dto.review.ReviewResponseDto;
import com.lms.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByCourse(@PathVariable Long courseId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.findAll(courseId, pageable));
    }

    @PostMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ReviewResponseDto> createReview(
            @AuthenticationPrincipal Long studentId,
            @PathVariable Long courseId,
            @Valid @RequestBody ReviewRequestDto reviewDto
    ) {
        return ResponseEntity.ok(reviewService.addReview(studentId, courseId, reviewDto));
    }
}
