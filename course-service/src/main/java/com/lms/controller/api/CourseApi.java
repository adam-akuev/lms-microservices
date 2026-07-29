package com.lms.controller.api;

import com.lms.dto.course.CourseRequest;
import com.lms.dto.course.CourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Курсы", description = "Управление курсами, поиск и каталог")
@SecurityRequirement(name = "bearerAuth")
public interface CourseApi {

    @Operation(
            summary = "Получить список курсов",
            description = "Возвращает пагинированный список курсов с поиском по текстовому запросу и фильтрацией по категории"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список курсов успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    ResponseEntity<Page<CourseResponse>> getCourses(String query, Long categoryId, Pageable pageable);

    @Operation(
            summary = "Получить курс по ID",
            description = "Возвращает подробную информацию о конкретном курсе"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Информация о курсе получена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    ResponseEntity<CourseResponse> getCourseById(Long id);

    @Operation(
            summary = "Получить курсы текущего студента",
            description = "Возвращает список всех курсов, на которые записан авторизованный студент"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Курсы студента успешно получены"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль STUDENT)")
    })
    ResponseEntity<List<CourseResponse>> getMyCourses(Long studentId);

    @Operation(
            summary = "Создать новый курс",
            description = "Создает новый курс (доступно преподавателям и администраторам)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Курс успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входящих данных"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль TEACHER или ADMIN)")
    })
    ResponseEntity<CourseResponse> create(CourseRequest request, Long currentUserId);

    @Operation(
            summary = "Обновить курс",
            description = "Обновляет данные существующего курса по его ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Курс успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входящих данных"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль TEACHER или ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    ResponseEntity<CourseResponse> update(Long id, CourseRequest request);

    @Operation(
            summary = "Удалить курс",
            description = "Удаляет курс по ID (доступно только администраторам)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Курс успешно удален"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    ResponseEntity<Void> delete(Long id);
}