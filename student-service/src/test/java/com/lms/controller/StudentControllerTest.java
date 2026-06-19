package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.student.StudentRequest;
import com.lms.dto.student.StudentResponse;
import com.lms.security.SecurityConfig;
import com.lms.security.JwtProvider;
import com.lms.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@Import(SecurityConfig.class) // Подгружаем твою конфигурацию безопасности
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @MockBean
    private JwtProvider jwtProvider; // Заглушка для корректного старта JwtFilter

    // --- Тесты для getMyProfile (GET /api/students/me) ---

    @Test
    @WithMockUser(roles = "STUDENT") // Студент может смотреть свой профиль
    void getMyProfile_Returns200_ForStudent() throws Exception {
        StudentResponse responseMock = new StudentResponse(1L, "Алексей Петров", "+79990001122", LocalDate.of(1998, 12, 1));

        // Используем any(), чтобы избежать проблем с сопоставлением @AuthenticationPrincipal Long
        when(studentService.getProfileById(any())).thenReturn(responseMock);

        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("Алексей Петров"))
                .andExpect(jsonPath("$.phone").value("+79990001122"))
                .andExpect(jsonPath("$.birthDate").value("1998-12-01"));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Админ тоже имеет доступ согласно hasAnyRole('STUDENT', 'ADMIN')
    void getMyProfile_Returns200_ForAdmin() throws Exception {
        StudentResponse responseMock = new StudentResponse(2L, "Администратор", null, null);
        when(studentService.getProfileById(any())).thenReturn(responseMock);

        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Администратор"));
    }

    @Test
    @WithMockUser(roles = "TEACHER") // Учителю доступ закрыт
    void getMyProfile_Returns403_ForTeacher() throws Exception {
        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isForbidden()); // 403 Forbidden

        verifyNoInteractions(studentService);
    }

    // --- Тесты для updateMyProfile (PUT /api/students/me) ---

    @Test
    @WithMockUser(roles = "STUDENT") // Студент обновляет свой профиль
    void updateMyProfile_Returns200_WhenValid() throws Exception {
        StudentRequest request = new StudentRequest("Новое Имя", "+79995554433", LocalDate.of(1999, 5, 20));
        StudentResponse responseMock = new StudentResponse(1L, "Новое Имя", "+79995554433", LocalDate.of(1999, 5, 20));

        when(studentService.updateProfile(any(), any(StudentRequest.class))).thenReturn(responseMock);

        mockMvc.perform(put("/api/students/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Новое Имя"))
                .andExpect(jsonPath("$.phone").value("+79995554433"));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Админ НЕ имеет права обновлять профиль (в PreAuthorize стоит только 'STUDENT')
    void updateMyProfile_Returns403_ForAdmin() throws Exception {
        StudentRequest request = new StudentRequest("Админ пытается менять", "+7000", null);

        mockMvc.perform(put("/api/students/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(studentService);
    }
}