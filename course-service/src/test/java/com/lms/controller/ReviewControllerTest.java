package com.lms.controller;

import com.lms.dto.review.ReviewRequestDto;
import com.lms.dto.review.ReviewResponseDto;
import com.lms.security.SecurityConfig;
import com.lms.security.JwtProvider;
import com.lms.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@Import(SecurityConfig.class) // Подтягиваем конфигурацию безопасности приложения
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JwtProvider jwtProvider; // Заглушка для успешного создания JwtFilter в контексте теста

    private ReviewResponseDto sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new ReviewResponseDto(
                100L,
                42L,
                5,
                "Шикарный курс, всё понятно!",
                LocalDateTime.now()
        );
    }

    // =========================================================================
    // ТЕСТЫ ДЛЯ GET /api/review/courses/{courseId}
    // =========================================================================

    @Test
    @WithMockUser(roles = "STUDENT")
    void getReviews_AuthAsStudent_ShouldReturn200() throws Exception {
        when(reviewService.findAll(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/review/courses/1")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.content[0].text").value("Шикарный курс, всё понятно!"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void getReviews_AuthAsTeacher_ShouldReturn200() throws Exception {
        when(reviewService.findAll(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/review/courses/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getReviews_AuthAsAdmin_ShouldReturn403() throws Exception {
        // У Админа нет доступа согласно @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
        mockMvc.perform(get("/api/review/courses/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReviews_Unauthorized_ShouldReturn401Or403() throws Exception {
        // Без токена/пользователя вообще доступ закрыт
        mockMvc.perform(get("/api/review/courses/1"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // ТЕСТЫ ДЛЯ POST /api/review/courses/{courseId}
    // =========================================================================

    @Test
    void createReview_Success_ShouldReturn200() throws Exception {
        ReviewRequestDto requestDto = new ReviewRequestDto(5, "Нормальный курс");

        // Используем any() для studentId, чтобы Mockito гарантированно перехватил кастомный кастинг принципала
        when(reviewService.addReview(any(), eq(1L), any(ReviewRequestDto.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/review/courses/1")
                        // Имитируем Principal со значением 42L (передается как имя пользователя) и ролью STUDENT
                        .with(user("42").roles("STUDENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.text").value("Шикарный курс, всё понятно!"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createReview_AsTeacher_ShouldReturn403() throws Exception {
        ReviewRequestDto requestDto = new ReviewRequestDto(5, "Я препод, но хочу оставить отзыв");

        // Роль TEACHER заблокирована аннотацией @PreAuthorize("hasRole('STUDENT')")
        mockMvc.perform(post("/api/review/courses/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reviewService);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createReview_InvalidDto_ShouldReturn400() throws Exception {
        // Невалидный DTO: пустой текст и рейтинг вне диапазона 1-5
        ReviewRequestDto invalidDto = new ReviewRequestDto(-1, "");
    }
}