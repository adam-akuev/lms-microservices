package com.lms.controller;

import com.lms.dto.TeacherResponse;
import com.lms.dto.internal.TeacherResponseInternal;
import com.lms.service.TeacherService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers/internal")
@RequiredArgsConstructor
@Hidden
public class TeacherControllerInternal {

    private final TeacherService teacherService;

    @GetMapping("/{id}/exists")
    public boolean existsById(@PathVariable Long id) {
        return teacherService.exists(id);
    }

    @GetMapping("/{id}")
    public TeacherResponseInternal getProfileById(@PathVariable Long id) {
        return teacherService.getProfileByIdInternal(id);
    }

    @GetMapping("/bulk")
    public List<TeacherResponseInternal> getProfilesByIds(@RequestParam List<Long> ids) {
        return teacherService.getProfilesByIds(ids);
    }
}
