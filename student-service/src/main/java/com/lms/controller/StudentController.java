package com.lms.controller;

import com.lms.dto.student.StudentRequest;
import com.lms.dto.student.StudentResponse;
import com.lms.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<StudentResponse> getMyProfile(@AuthenticationPrincipal Long studentId) {
        return ResponseEntity.ok(studentService.getProfileById(studentId));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponse> updateMyProfile(
            @AuthenticationPrincipal Long studentId,
            @RequestBody @Valid StudentRequest request
    ) {
        return ResponseEntity.ok(studentService.updateProfile(studentId, request));
    }
}
