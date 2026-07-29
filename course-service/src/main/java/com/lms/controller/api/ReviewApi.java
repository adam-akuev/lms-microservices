package com.lms.controller.api;

import com.lms.dto.review.ReviewRequestDto;
import com.lms.dto.review.ReviewResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Отзывы", description = "Управление отзывами и оценками курсов")
@SecurityRequirement(name = "bearerAuth")
public interface ReviewApi {

    @Operation(
            summary = "Получить отзывы к курсу",
            description = "Возвращает пагинированный список отзывов для указанного курса"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список отзывов успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль STUDENT или TEACHER)"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    ResponseEntity<Page<ReviewResponseDto>> getReviewsByCourse(Long courseId, Pageable pageable);

    @Operation(
            summary = "Оставить отзыв к курсу",
            description = "Создает новый отзыв и оценку к курсу от имени авторизованного студента"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Отзыв успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или отзыв к этому курсу уже был оставлен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль STUDENT)"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    ResponseEntity<ReviewResponseDto> createReview(Long studentId, Long courseId, ReviewRequestDto reviewDto);
}