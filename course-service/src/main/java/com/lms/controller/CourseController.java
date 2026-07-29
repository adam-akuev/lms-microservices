package com.lms.controller;

import com.lms.controller.api.CourseApi;
import com.lms.dto.course.CourseRequest;
import com.lms.dto.course.CourseResponse;
import com.lms.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController implements CourseApi {
    private final CourseService courseService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CourseResponse>> getCourses(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(courseService.searchCourses(query, categoryId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(courseService.getById(id));
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CourseResponse>> getMyCourses(@AuthenticationPrincipal Long studentId) {
        return ResponseEntity.ok(courseService.getStudentCourses(studentId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request,
                                                 @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.status(201).body(courseService.create(request, currentUserId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<CourseResponse> update(@PathVariable("id") Long id,
                                                 @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
