package com.lms.controller.internal;

import com.lms.dto.internal.course.CourseInternalResponse;
import com.lms.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses/internal")
@RequiredArgsConstructor
public class CourseInternalController {

    private final CourseService courseService;

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseInternalResponse> getCourseById(@PathVariable("courseId") Long courseId) {
        var course = courseService.getById(courseId);
        return ResponseEntity.ok(new CourseInternalResponse(course.id(), course.title()));
    }
}