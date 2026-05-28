package com.lms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "student-service", contextId = "internalEnrollmentClient")
public interface EnrollmentClient {

    @DeleteMapping("/api/enrollments/internal/courses/{courseId}")
    void deleteEnrollmentsByCourseId(@PathVariable("courseId") Long courseId);

    @GetMapping("/api/enrollments/internal/check")
    boolean checkEnrollment(
            @RequestParam("studentId") Long studentId,
            @RequestParam("courseId") Long courseId
    );
}
