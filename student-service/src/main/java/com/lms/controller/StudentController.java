package com.lms.controller;

import com.lms.dto.StudentRequest;
import com.lms.dto.StudentResponse;
import com.lms.security.JwtProvider;
import com.lms.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;
    private final JwtProvider jwtProvider;

    @GetMapping("/me")
    public ResponseEntity<StudentResponse> getMyProfile(@RequestHeader("Authorization") String token) {
        Long studentId = jwtProvider.getIdFromToken(token.substring(7));
        return ResponseEntity.ok(studentService.getProfileById(studentId));
    }

    @PutMapping("/me")
    public ResponseEntity<StudentResponse> updateMyProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid StudentRequest request
    ) {
        Long studentId = jwtProvider.getIdFromToken(token.substring(7));
        return ResponseEntity.ok(studentService.updateProfile(studentId, request));
    }
}
