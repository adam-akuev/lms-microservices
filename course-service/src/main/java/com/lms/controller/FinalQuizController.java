package com.lms.controller;

import com.lms.controller.api.FinalQuizApi;
import com.lms.dto.quiz.*;
import com.lms.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/quiz")
@RequiredArgsConstructor
public class FinalQuizController implements FinalQuizApi {

    private final QuizService finalQuizService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentQuizDto> getFinalQuiz(
            @PathVariable("courseId") Long courseId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(finalQuizService.getFinalQuiz(courseId, userId));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizResultDto> submitQuiz(
            @PathVariable("courseId") Long courseId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SubmitQuizRequest request
    ) {
        return ResponseEntity.ok(finalQuizService.submitQuiz(courseId, userId, request));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<QuizResponse> createQuiz(
            @PathVariable("courseId") Long courseId,
            @Valid @RequestBody CreateQuizRequest request
    ) {
        QuizResponse response = finalQuizService.createQuiz(courseId, request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable("courseId") Long courseId,
            @Valid @RequestBody CreateQuizRequest request
    ) {
        return ResponseEntity.ok(finalQuizService.updateQuiz(courseId, request));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable("courseId") Long courseId) {
        finalQuizService.deleteQuiz(courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/attempts")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<QuizAttemptResponse>> getMyAttempts(
            @PathVariable("courseId") Long courseId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(finalQuizService.getMyAttempts(courseId, userId));
    }

    @GetMapping("/manage")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<QuizAdminDto> getQuizForManagement(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(finalQuizService.getQuizForManagement(courseId));
    }
}