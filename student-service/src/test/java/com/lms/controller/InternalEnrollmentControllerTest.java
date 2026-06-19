package com.lms.controller;

import com.lms.security.SecurityConfig;
import com.lms.security.JwtProvider;
import com.lms.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content; // Важный импорт для проверки контента

@WebMvcTest(InternalEnrollmentController.class)
@Import(SecurityConfig.class)
class InternalEnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private JwtProvider jwtProvider;

    // --- Тест для checkEnrollment ---

    @Test
    void checkEnrollment_ReturnsTrue_WhenStudentIsEnrolled() throws Exception {
        Long studentId = 1L;
        Long courseId = 10L;

        when(enrollmentService.isStudentEnrolled(studentId, courseId)).thenReturn(true);

        mockMvc.perform(get("/api/enrollments/internal/check")
                        .param("studentId", studentId.toString())
                        .param("courseId", courseId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(enrollmentService, times(1)).isStudentEnrolled(studentId, courseId);
    }

    // --- Тест для getStudentCourseIds ---

    @Test
    void getStudentCourseIds_ReturnsList() throws Exception {
        Long studentId = 1L;
        List<Long> expectedCourseIds = List.of(10L, 20L, 30L);

        when(enrollmentService.getStudentCourseIds(studentId)).thenReturn(expectedCourseIds);

        mockMvc.perform(get("/api/enrollments/internal/student/{studentId}/courses", studentId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)) // Исправлено: теперь вызывается через content()
                .andExpect(jsonPath("$[0]").value(10L))
                .andExpect(jsonPath("$[1]").value(20L))
                .andExpect(jsonPath("$[2]").value(30L));

        verify(enrollmentService, times(1)).getStudentCourseIds(studentId);
    }
}