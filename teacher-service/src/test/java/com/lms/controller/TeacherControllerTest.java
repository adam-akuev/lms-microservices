package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.TeacherRequest;
import com.lms.dto.TeacherResponse;
import com.lms.security.SecurityConfig;
import com.lms.security.JwtProvider;
import com.lms.service.TeacherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeacherController.class)
@Import(SecurityConfig.class)
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private JwtProvider jwtProvider;

    // --- Тесты для getTeachers (GET /api/teachers) ---

    @Test
    @WithMockUser // Любой аутентифицированный пользователь имеет доступ
    void getTeachers_Returns200AndPage_WhenAuthenticated() throws Exception {
        TeacherResponse teacherMock = new TeacherResponse(
                1L, "Сергей Петров", "+79991112233", LocalDate.of(1980, 1, 1), 
                "Опыт разработки", "Senior Java Developer", 12
        );
        Page<TeacherResponse> pageMock = new PageImpl<>(List.of(teacherMock));

        when(teacherService.getTeachersWithFilter(eq("Java"), any(Pageable.class))).thenReturn(pageMock);

        mockMvc.perform(get("/api/teachers")
                        .param("qualification", "Java")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].fullName").value("Сергей Петров"))
                .andExpect(jsonPath("$.content[0].qualification").value("Senior Java Developer"));
    }

    @Test
    void getTeachers_Returns401_WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isUnauthorized()); // Без пользователя срабатывает фильтр и возвращает 401
    }

    // --- Тесты для getMyProfile (GET /api/teachers/me) ---

    @Test
    @WithMockUser(roles = "TEACHER")
    void getMyProfile_Returns200_ForTeacher() throws Exception {
        TeacherResponse teacherMock = new TeacherResponse(1L, "Сергей Петров", null, null, null, null, null);
        when(teacherService.getProfileById(any())).thenReturn(teacherMock);

        mockMvc.perform(get("/api/teachers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Сергей Петров"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getMyProfile_Returns403_ForStudent() throws Exception {
        mockMvc.perform(get("/api/teachers/me"))
                .andExpect(status().isForbidden());
    }

    // --- Тесты для updateProfile (PUT /api/teachers/me) ---

    @Test
    @WithMockUser(roles = "TEACHER")
    void updateProfile_Returns200_WhenValid() throws Exception {
        TeacherRequest request = new TeacherRequest("Новое Имя", "+7000", null, "Bio", "Java", 5);
        TeacherResponse responseMock = new TeacherResponse(1L, "Новое Имя", "+7000", null, "Bio", "Java", 5);

        when(teacherService.updateProfile(any(), any(TeacherRequest.class))).thenReturn(responseMock);

        mockMvc.perform(put("/api/teachers/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Новое Имя"))
                .andExpect(jsonPath("$.qualification").value("Java"));
    }

    // --- Тесты для deactivateTeacher (PUT /api/teachers/{id}/deactivate) ---

    @Test
    @WithMockUser(roles = "ADMIN") // Только админ деактивирует аккаунты
    void deactivateTeacher_Returns204_ForAdmin() throws Exception {
        Long teacherId = 5L;
        doNothing().when(teacherService).deactivateProfile(teacherId);

        mockMvc.perform(put("/api/teachers/{id}/deactivate", teacherId))
                .andExpect(status().isNoContent()); // 204 No Content

        verify(teacherService, times(1)).deactivateProfile(teacherId);
    }

    @Test
    @WithMockUser(roles = "TEACHER") // Сам преподаватель не может себя деактивировать через этот эндпоинт
    void deactivateTeacher_Returns403_ForTeacher() throws Exception {
        mockMvc.perform(put("/api/teachers/5/deactivate"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(teacherService);
    }
}