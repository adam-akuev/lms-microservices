package com.lms.controller;

import com.lms.dto.internal.CreateStudentProfileRequest;
import com.lms.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students/internal")
@RequiredArgsConstructor
public class InternalStudentController {

    private final StudentService studentService;

    @PostMapping("/create")
    public ResponseEntity<Void> createProfileInternal(@RequestBody CreateStudentProfileRequest request) {
        studentService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
