package com.lms.controller;

import com.lms.dto.progress.CourseProgressResponse;
import com.lms.service.LessonProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class LessonProgressController {

    private final LessonProgressService lessonProgressService;

    @PostMapping("/lessons/{lessonId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Integer> completeLesson(
            @AuthenticationPrincipal Long studentId,
            @PathVariable Long lessonId
    ) {
        Integer progress = lessonProgressService.completeLesson(studentId, lessonId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @AuthenticationPrincipal Long studentId,
            @PathVariable Long courseId
    ) {
        CourseProgressResponse progress = lessonProgressService.getCourseProgressDetails(studentId, courseId);
        return ResponseEntity.ok(progress);
    }


}
