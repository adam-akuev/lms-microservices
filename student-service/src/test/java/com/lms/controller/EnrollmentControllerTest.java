package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.enrollment.EnrollmentRequest;
import com.lms.dto.enrollment.EnrollmentResponse;
import com.lms.security.SecurityConfig;
import com.lms.security.JwtProvider;
import com.lms.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
@Import(SecurityConfig.class) // Наш неизменный конфиг безопасности
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private JwtProvider jwtProvider; // Мок фильтра безопасности

    @Test
    @WithMockUser(roles = "STUDENT") // Только студент имеет право записаться
    void enrollMe_Returns200_WhenValidRequest() throws Exception {
        Long courseId = 42L;
        EnrollmentRequest request = new EnrollmentRequest(courseId);
        
        // Предполагаем структуру EnrollmentResponse (id, studentId, courseId, enrolledAt)
        EnrollmentResponse responseMock = new EnrollmentResponse(100L, 1L, courseId, LocalDateTime.now());

        // Используем any() вместо studentId, так как @AuthenticationPrincipal в тесте подменится строкой
        when(enrollmentService.enroll(any(), eq(courseId))).thenReturn(responseMock);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.courseId").value(42L));

        verify(enrollmentService, times(1)).enroll(any(), eq(courseId));
    }

    @Test
    @WithMockUser(roles = "TEACHER") // Учителям записываться на курсы запрещено
    void enrollMe_Returns403_ForTeacher() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest(42L);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // 403 Forbidden

        verifyNoInteractions(enrollmentService);
    }
}