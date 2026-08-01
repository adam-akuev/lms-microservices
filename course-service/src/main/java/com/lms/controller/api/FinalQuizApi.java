package com.lms.controller.api;

import com.lms.dto.quiz.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Финальный тест курса", description = "Прохождение и управление финальным тестом курса")
@SecurityRequirement(name = "bearerAuth")
public interface FinalQuizApi {

    @Operation(
            summary = "Получить финальный тест курса",
            description = "Возвращает вопросы и варианты ответов без указания правильного варианта. " +
                    "Доступно только после прохождения всех уроков курса."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тест успешно получен"),
            @ApiResponse(responseCode = "400", description = "Не все уроки курса пройдены"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль STUDENT)"),
            @ApiResponse(responseCode = "404", description = "Тест для данного курса не найден")
    })
    ResponseEntity<StudentQuizDto> getFinalQuiz(Long courseId, Long userId);

    @Operation(
            summary = "Отправить ответы на финальный тест",
            description = "Проверяет ответы студента, сохраняет попытку и возвращает результат с баллом"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тест проверен, результат возвращен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль STUDENT)"),
            @ApiResponse(responseCode = "404", description = "Тест не найден")
    })
    ResponseEntity<QuizResultDto> submitQuiz(Long courseId, Long userId, SubmitQuizRequest request);

    @Operation(
            summary = "Создать финальный тест для курса",
            description = "Создает тест с вопросами и вариантами ответов (доступно преподавателям и администраторам)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Тест успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или тест уже существует"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль TEACHER или ADMIN)")
    })
    ResponseEntity<QuizResponse> createQuiz(Long courseId, CreateQuizRequest request);

    @Operation(
            summary = "Обновить финальный тест курса",
            description = "Полностью заменяет структуру теста: название, проходной балл, вопросы и варианты ответов"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тест успешно обновлён"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входящих данных"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль TEACHER или ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Тест для данного курса не найден")
    })
    ResponseEntity<QuizResponse> updateQuiz(Long courseId, CreateQuizRequest request);

    @Operation(
            summary = "Удалить финальный тест курса",
            description = "Удаляет тест вместе со всеми вопросами и вариантами ответов (доступно только администраторам)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Тест успешно удалён"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Тест не найден")
    })
    ResponseEntity<Void> deleteQuiz(Long courseId);

    @Operation(
            summary = "Получить историю попыток текущего студента",
            description = "Возвращает список всех попыток сдачи финального теста для данного курса"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "История попыток получена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль STUDENT)"),
            @ApiResponse(responseCode = "404", description = "Тест для данного курса не найден")
    })
    ResponseEntity<List<QuizAttemptResponse>> getMyAttempts(Long courseId, Long userId);

    @Operation(
            summary = "Получить тест для редактирования",
            description = "Возвращает полную структуру теста, включая правильные ответы (для преподавателя/админа)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тест успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль TEACHER или ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Тест для данного курса не найден")
    })
    ResponseEntity<QuizAdminDto> getQuizForManagement(Long courseId);
}