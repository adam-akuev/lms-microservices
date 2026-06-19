package com.lms.controller;

import com.lms.security.SecurityConfig;
import com.lms.security.JwtProvider;
import com.lms.service.LessonProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LessonProgressController.class)
@Import(SecurityConfig.class) // Подтягиваем наш конфиг безопасности
class LessonProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LessonProgressService lessonProgressService;

    @MockBean
    private JwtProvider jwtProvider; // Заглушка для корректной инициализации JwtFilter

    // --- Тесты для эндпоинта completeLesson (POST) ---

    @Test
    @WithMockUser(roles = "STUDENT")
    void completeLesson_Returns200_ForStudent() throws Exception {
        Long lessonId = 15L;
        int expectedProgress = 50;

        // Испольуем any() вместо anyLong(), чтобы Mockito сматчил аргумент, даже если там пришел null или кастомный принципал
        when(lessonProgressService.completeLesson(any(), eq(lessonId))).thenReturn(expectedProgress);

        mockMvc.perform(post("/api/progress/lessons/{lessonId}/complete", lessonId))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));
    }

    @Test
    @WithMockUser(roles = "TEACHER") // Учитель ПЫТАЕТСЯ отметить урок как завершенный
    void completeLesson_Returns403_ForTeacher() throws Exception {
        mockMvc.perform(post("/api/progress/lessons/15/complete"))
                .andExpect(status().isForbidden()); // 403 Forbidden, роль TEACHER не подходит

        verifyNoInteractions(lessonProgressService);
    }

    // --- Тесты для эндпоинта getCourseProgress (GET) ---

    @Test
    @WithMockUser(roles = "STUDENT")
    void getCourseProgress_Returns200_ForStudent() throws Exception {
        Long courseId = 20L;
        // Заменяем anyLong() на any()
        when(lessonProgressService.calculateCourseProgress(any(), eq(courseId))).thenReturn(75);

        mockMvc.perform(get("/api/progress/courses/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(content().string("75"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCourseProgress_Returns200_ForAdmin() throws Exception {
        Long courseId = 20L;
        // Заменяем anyLong() на any()
        when(lessonProgressService.calculateCourseProgress(any(), eq(courseId))).thenReturn(100);

        mockMvc.perform(get("/api/progress/courses/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    @WithMockUser(roles = "TEACHER") // Учителю роль не прописали в hasAnyRole('STUDENT', 'ADMIN')
    void getCourseProgress_Returns403_ForTeacher() throws Exception {
        mockMvc.perform(get("/api/progress/courses/20"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(lessonProgressService);
    }
}