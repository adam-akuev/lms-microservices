package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.lesson.LessonRequest;
import com.lms.dto.lesson.LessonResponse;
import com.lms.security.SecurityConfig;
import com.lms.security.JwtProvider;
import com.lms.service.LessonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LessonController.class)
@Import(SecurityConfig.class) // Подгружаем твою конфигурацию безопасности
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonService lessonService;

    // Закрываем JwtProvider заглушкой, так как он нужен и для JwtFilter, и для самого контроллера
    @MockBean
    private JwtProvider jwtProvider;

    // --- Тесты авторизации и ролей ---

    @Test
    @WithMockUser(roles = "STUDENT") // Студент имеет право запрашивать уроки курса
    void getAllLessonsOfCourse_Returns200_ForStudent() throws Exception {
        Long courseId = 10L;
        LessonResponse lessonMock = new LessonResponse(100L, "Введение в Java", "Контент");
        
        when(lessonService.getAllLessonsForStudentByCourseId(any(), eq(courseId)))
                .thenReturn(List.of(lessonMock));

        mockMvc.perform(get("/api/lessons/course/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].title").value("Введение в Java"));
    }

    @Test
    void getAllLessonsOfCourse_Returns401_WhenUnauthorized() throws Exception {
        // Вызов БЕЗ @WithMockUser симулирует неавторизованного пользователя
        mockMvc.perform(get("/api/lessons/course/10"))
                .andExpect(status().isUnauthorized()); // Ждем 401 Unauthorized согласно правилам Spring Security

        verifyNoInteractions(lessonService);
    }

    // --- Тесты CRUD-операций ---

    @Test
    @WithMockUser(roles = "TEACHER") // Учитель может создавать уроки
    void create_Returns201_WhenUserIsTeacher() throws Exception {
        LessonRequest request = new LessonRequest("Новый урок", "Интересный контент", 10L);
        LessonResponse responseMock = new LessonResponse(100L, "Новый урок", "Интересный контент");

        when(lessonService.create(any(LessonRequest.class))).thenReturn(responseMock);

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.title").value("Новый урок"));
    }

    @Test
    @WithMockUser(roles = "STUDENT") // Студент НЕ может создавать уроки
    void create_Returns403_WhenUserIsStudent() throws Exception {
        LessonRequest request = new LessonRequest("Студент пробует хакнуть", "Контент", 10L);

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // 403 Forbidden

        verifyNoInteractions(lessonService);
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Админ может обновлять уроки
    void update_Returns200_WhenUserIsAdmin() throws Exception {
        Long lessonId = 100L;
        LessonRequest request = new LessonRequest("Обновленный заголовок", "Новый контент", 10L);
        LessonResponse responseMock = new LessonResponse(lessonId, "Обновленный заголовок", "Новый контент");

        when(lessonService.update(eq(lessonId), any(LessonRequest.class))).thenReturn(responseMock);

        mockMvc.perform(put("/api/lessons/{id}", lessonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Обновленный заголовок"));
    }

    @Test
    @WithMockUser(roles = "TEACHER") // Учитель может удалять уроки
    void delete_Returns204_WhenUserIsTeacher() throws Exception {
        Long lessonId = 100L;
        doNothing().when(lessonService).delete(lessonId);

        mockMvc.perform(delete("/api/lessons/{id}", lessonId))
                .andExpect(status().isNoContent()); // 204 No Content

        verify(lessonService, times(1)).delete(lessonId);
    }
}