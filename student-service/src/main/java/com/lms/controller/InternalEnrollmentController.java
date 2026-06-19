package com.lms.controller;

import com.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments/internal")
@RequiredArgsConstructor
public class InternalEnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/check")
    public boolean checkEnrollment(
            @RequestParam Long studentId,
            @RequestParam Long courseId
    ) {
        return enrollmentService.isStudentEnrolled(studentId, courseId);
    }

    @GetMapping("/student/{studentId}/courses")
    public List<Long> getStudentCourseIds(@PathVariable Long studentId) {
        return enrollmentService.getStudentCourseIds(studentId);
    }
}
