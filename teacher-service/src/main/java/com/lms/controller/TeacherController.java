package com.lms.controller;

import com.lms.dto.TeacherRequest;
import com.lms.dto.TeacherResponse;
import com.lms.security.JwtProvider;
import com.lms.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TeacherResponse>> getTeachers(
            @RequestParam(required = false) String qualification,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(teacherService.getTeachersWithFilter(qualification, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherResponse> getMyProfile(@AuthenticationPrincipal Long teacherId) {
        return ResponseEntity.ok(teacherService.getProfileById(teacherId));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherResponse> updateProfile(@AuthenticationPrincipal Long teacherId,
                                                         @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(teacherService.updateProfile(teacherId, request));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateTeacher(@PathVariable Long id) {
        teacherService.deactivateProfile(id);
        return ResponseEntity.noContent().build();
    }
}
