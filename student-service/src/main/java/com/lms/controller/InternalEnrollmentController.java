package com.lms.controller;

import com.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments/internal")
@RequiredArgsConstructor
public class InternalEnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkEnrollment(
            @RequestParam Long studentId,
            @RequestParam Long courseId
    ) {
        return ResponseEntity.ok(enrollmentService.isStudentEnrolled(studentId, courseId));
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteEnrollmentByCourseId(@PathVariable("courseId") Long courseId) {
        enrollmentService.cascadeDeleteEnrollments(courseId);
        return ResponseEntity.noContent().build();
    }
}
