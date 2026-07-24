package com.lms.controller.api;

import com.lms.dto.lesson.LessonRequest;
import com.lms.dto.lesson.LessonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Уроки", description = "Управление уроками и материалами курса")
@SecurityRequirement(name = "bearerAuth")
public interface LessonApi {

    @Operation(
            summary = "Получить все уроки курса",
            description = "Возвращает список уроков конкретного курса для авторизованного пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список уроков успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к материалам данного курса"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    ResponseEntity<List<LessonResponse>> getAllLessonsOfCourse(Long userId, Long courseId);

    @Operation(
            summary = "Получить урок по ID",
            description = "Возвращает подробную информацию о конкретном уроке"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Информация об уроке успешно получена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Урок не найден")
    })
    ResponseEntity<LessonResponse> getLessonById(Long id);

    @Operation(
            summary = "Создать новый урок",
            description = "Добавляет новый урок в курс (доступно преподавателям и администраторам)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Урок успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входящих данных"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль TEACHER или ADMIN)")
    })
    ResponseEntity<LessonResponse> create(LessonRequest request);

    @Operation(
            summary = "Обновить урок",
            description = "Обновляет данные существующего урока по его ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Урок успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входящих данных"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль TEACHER или ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Урок не найден")
    })
    ResponseEntity<LessonResponse> update(Long id, LessonRequest request);

    @Operation(
            summary = "Удалить урок",
            description = "Удаляет урок по ID (доступно преподавателям и администраторам)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Урок успешно удален"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль TEACHER или ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Урок не найден")
    })
    ResponseEntity<Void> delete(Long id);
}