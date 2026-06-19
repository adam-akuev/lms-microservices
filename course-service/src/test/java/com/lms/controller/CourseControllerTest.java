package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.course.CourseRequest;
import com.lms.dto.course.CourseResponse;
import com.lms.dto.internal.teacher.TeacherResponseInternal;
import com.lms.security.SecurityConfig; // Импортируем твой конфиг безопасности
import com.lms.security.JwtProvider; // Импортируем провайдер
import com.lms.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import(SecurityConfig.class) // Явно импортируем твою конфигурацию безопасности в контекст теста
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    // Этот MockBean создаст фейковый JwtProvider,
    // который Spring успешно внедрит в твой JwtFilter при старте теста
    @MockBean
    private JwtProvider jwtProvider;

    // --- Тесты безопасности (Security & Roles) ---

    @Test
    @WithMockUser(roles = "STUDENT") // Симулируем студента
    void getMyCourses_Returns200_ForStudent() throws Exception {
        CourseResponse courseMock = new CourseResponse(1L, "Java", "Desc", BigDecimal.TEN, null, 0);
        when(courseService.getStudentCourses(any())).thenReturn(List.of(courseMock));

        mockMvc.perform(get("/api/courses/my-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java"));
    }

    @Test
    @WithMockUser(roles = "TEACHER") // Учитель ловит 403, так как эндпоинт только для студентов
    void getMyCourses_Returns403_ForTeacher() throws Exception {
        mockMvc.perform(get("/api/courses/my-courses"))
                .andExpect(status().isForbidden()); // 403 Forbidden
        
        verifyNoInteractions(courseService);
    }

    // --- Тесты CRUD-операций ---

    @Test
    @WithMockUser(roles = "TEACHER")
    void create_Returns201_WhenValid() throws Exception {
        // Arrange
        CourseRequest request = new CourseRequest("Spring Boot", "Описание", BigDecimal.valueOf(100), 5L);
        TeacherResponseInternal teacher = new TeacherResponseInternal(5L, "Имя", null, null);
        CourseResponse expectedResponse = new CourseResponse(10L, "Spring Boot", "Описание", BigDecimal.valueOf(100), teacher, null);

        when(courseService.create(any(CourseRequest.class), any())).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/courses")
                        .with(csrf()) // Включаем CSRF-токен, если он активирован в конфигурации защиты
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Проверяем статус 201
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Spring Boot"))
                .andExpect(jsonPath("$.teacher.id").value(5L));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void update_Returns200() throws Exception {
        Long courseId = 1L;
        CourseRequest request = new CourseRequest("Updated Title", "Desc", BigDecimal.TEN, 5L);
        CourseResponse expectedResponse = new CourseResponse(courseId, "Updated Title", "Desc", BigDecimal.TEN, null, null);

        when(courseService.update(eq(courseId), any(CourseRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(put("/api/courses/{id}", courseId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Только админ имеет право удалять
    void delete_Returns204_ForAdmin() throws Exception {
        Long courseId = 1L;
        doNothing().when(courseService).delete(courseId);

        mockMvc.perform(delete("/api/courses/{id}", courseId)
                        .with(csrf()))
                .andExpect(status().isNoContent()); // 204 No Content

        verify(courseService, times(1)).delete(courseId);
    }

    @Test
    @WithMockUser(roles = "STUDENT") // Студент пытается удалить курс
    void delete_Returns403_ForStudent() throws Exception {
        mockMvc.perform(delete("/api/courses/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(courseService, never()).delete(anyLong());
    }
}