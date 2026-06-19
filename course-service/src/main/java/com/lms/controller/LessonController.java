package com.lms.controller;

import com.lms.dto.lesson.LessonRequest;
import com.lms.dto.lesson.LessonResponse;
import com.lms.security.JwtProvider;
import com.lms.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final JwtProvider jwtProvider;

    @GetMapping("/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LessonResponse>> getAllLessonsOfCourse(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(lessonService.getAllLessonsForStudentByCourseId(userId, courseId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<LessonResponse> create(@RequestBody @Valid LessonRequest request) {
        return ResponseEntity.status(201).body(lessonService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<LessonResponse> update(@PathVariable Long id, @RequestBody @Valid LessonRequest request) {
        return ResponseEntity.ok(lessonService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
