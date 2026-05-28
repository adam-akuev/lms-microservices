package com.lms.client;

import com.lms.dto.internal.CreateStudentProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "student-service")
public interface StudentClient {

    @PostMapping("/api/students/internal/create")
    ResponseEntity<Void> createProfile(@RequestBody CreateStudentProfileRequest request);
}
