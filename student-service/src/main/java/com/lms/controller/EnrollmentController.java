package com.lms.controller;

import com.lms.dto.enrollment.EnrollmentRequest;
import com.lms.dto.enrollment.EnrollmentResponse;
import com.lms.security.JwtProvider;
import com.lms.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final JwtProvider jwtProvider;

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enrollMe(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid EnrollmentRequest request
    ) {
        Long studentId = jwtProvider.getIdFromToken(token.substring(7));
        EnrollmentResponse response = enrollmentService.enroll(studentId, request.courseId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-courses")
    public ResponseEntity<List<Long>> getMyCourseIds(@RequestHeader("Authorization") String token) {
        Long studentId = jwtProvider.getIdFromToken(token.substring(7));
        return ResponseEntity.ok(enrollmentService.getStudentCourseIds(studentId));
    }
}
