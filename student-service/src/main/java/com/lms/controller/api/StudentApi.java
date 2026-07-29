package com.lms.controller.api;

import com.lms.dto.student.StudentRequest;
import com.lms.dto.student.StudentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Студенты", description = "Управление профилем и данными студентов")
@SecurityRequirement(name = "bearerAuth")
public interface StudentApi {

    @Operation(summary = "Получить профиль текущего студента", description = "Возвращает данные профиля авторизованного студента на основе JWT-токена")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение профиля"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль STUDENT или ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Студент не найден")
    })
    ResponseEntity<StudentResponse> getMyProfile(Long studentId);

    @Operation(summary = "Обновить профиль текущего студента", description = "Обновляет личные данные студента")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входящих данных"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль STUDENT)")
    })
    ResponseEntity<StudentResponse> updateMyProfile(Long studentId, StudentRequest request);
}