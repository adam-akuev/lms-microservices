package com.lms.controller;

import com.lms.dto.internal.TeacherResponseInternal;
import com.lms.security.SecurityConfig;
import com.lms.security.JwtProvider;
import com.lms.service.TeacherService;
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

@WebMvcTest(TeacherControllerInternal.class)
@Import(SecurityConfig.class)
class TeacherControllerInternalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private JwtProvider jwtProvider;

    // --- Тест для existsById ---

    @Test
    void existsById_ReturnsTrue_WhenTeacherExists() throws Exception {
        Long teacherId = 1L;
        when(teacherService.exists(teacherId)).thenReturn(true);

        mockMvc.perform(get("/api/teachers/internal/{id}/exists", teacherId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(teacherService, times(1)).exists(teacherId);
    }

    // --- Тест для getProfileById ---

    @Test
    void getProfileById_ReturnsInternalProfile() throws Exception {
        Long teacherId = 1L;
        // Передаем параметры строго по твоему рекорду: id, fullName, qualification, experienceYears
        TeacherResponseInternal mockResponse = new TeacherResponseInternal(teacherId, "Игорь Петров", "Java Senior", 8);

        when(teacherService.getProfileByIdInternal(teacherId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/teachers/internal/{id}", teacherId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(teacherId))
                .andExpect(jsonPath("$.fullName").value("Игорь Петров"))
                .andExpect(jsonPath("$.qualification").value("Java Senior"))
                .andExpect(jsonPath("$.experienceYears").value(8));

        verify(teacherService, times(1)).getProfileByIdInternal(teacherId);
    }

    // --- Тест для getProfilesByIds (Bulk) ---

    @Test
    void getProfilesByIds_ReturnsListOfProfiles() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        List<TeacherResponseInternal> mockList = List.of(
                new TeacherResponseInternal(1L, "Преподаватель 1", "Kotlin", 3),
                new TeacherResponseInternal(2L, "Преподаватель 2", "Go", 5)
        );

        when(teacherService.getProfilesByIds(ids)).thenReturn(mockList);

        mockMvc.perform(get("/api/teachers/internal/bulk")
                        .param("ids", "1,2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[0].fullName").value("Преподаватель 1"))
                .andExpect(jsonPath("$[0].qualification").value("Kotlin"));

        verify(teacherService, times(1)).getProfilesByIds(ids);
    }
}