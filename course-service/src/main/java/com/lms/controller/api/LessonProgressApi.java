package com.lms.controller.api;

import com.lms.dto.progress.LessonProgressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Прогресс обучения", description = "Отслеживание и фиксация прогресса прохождения уроков и курсов")
@SecurityRequirement(name = "bearerAuth")
public interface LessonProgressApi {

    @Operation(
            summary = "Отметить урок как пройденный",
            description = "Фиксирует завершение урока авторизованным студентом и возвращает обновленный процент прогресса по курсу"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Урок успешно отмечен как пройденный"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль STUDENT)"),
            @ApiResponse(responseCode = "404", description = "Урок не найден")
    })
    ResponseEntity<Integer> completeLesson(Long studentId, Long lessonId);

    @Operation(
            summary = "Получить прогресс по курсу",
            description = "Возвращает детализированную информацию о прогрессе выполнения курса для студента"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Информация о прогрессе успешно получена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль STUDENT или ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    ResponseEntity<LessonProgressResponse> getCourseProgress(Long studentId, Long courseId);
}