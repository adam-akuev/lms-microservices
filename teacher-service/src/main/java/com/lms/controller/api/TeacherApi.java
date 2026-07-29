package com.lms.controller.api;

import com.lms.dto.TeacherRequest;
import com.lms.dto.TeacherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Преподаватели", description = "Управление профилями и просмотр списка преподавателей")
@SecurityRequirement(name = "bearerAuth")
public interface TeacherApi {

    @Operation(
            summary = "Получить список преподавателей",
            description = "Возвращает пагинированный список преподавателей с опциональной фильтрацией по квалификации"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список преподавателей успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    ResponseEntity<Page<TeacherResponse>> getTeachers(String qualification, Pageable pageable);

    @Operation(
            summary = "Получить профиль текущего преподавателя",
            description = "Возвращает данные профиля авторизованного преподавателя на основе JWT-токена"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль TEACHER)"),
            @ApiResponse(responseCode = "404", description = "Преподаватель не найден")
    })
    ResponseEntity<TeacherResponse> getMyProfile(Long teacherId);

    @Operation(
            summary = "Обновить профиль текущего преподавателя",
            description = "Обновляет личные данные авторизованного преподавателя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входящих данных"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль TEACHER)")
    })
    ResponseEntity<TeacherResponse> updateProfile(Long teacherId, TeacherRequest request);

    @Operation(
            summary = "Деактивировать преподавателя",
            description = "Переводит профиль преподавателя в деактивированный статус (доступно только администраторам)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Преподаватель успешно деактивирован"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Преподаватель не найден")
    })
    ResponseEntity<Void> deactivateTeacher(Long id);
}