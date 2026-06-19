package com.lms.controller;

import com.lms.dto.enrollment.EnrollmentRequest;
import com.lms.dto.enrollment.EnrollmentResponse;
import com.lms.security.JwtProvider;
import com.lms.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentResponse> enrollMe(
            @AuthenticationPrincipal Long studentId,
            @RequestBody @Valid EnrollmentRequest request
    ) {
        EnrollmentResponse response = enrollmentService.enroll(studentId, request.courseId());
        return ResponseEntity.ok(response);
    }
}
