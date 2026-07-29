package com.lms.controller.api;

import com.lms.dto.enrollment.EnrollmentRequest;
import com.lms.dto.enrollment.EnrollmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Записи на курсы", description = "Управление зачислениями студентов на курсы")
@SecurityRequirement(name = "bearerAuth")
public interface EnrollmentApi {

    @Operation(
            summary = "Записаться на курс",
            description = "Создает запись авторизованного студента на выбранный курс"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Студент успешно записан на курс"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или студент уже записан на курс"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль STUDENT)"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    ResponseEntity<EnrollmentResponse> enrollMe(Long studentId, EnrollmentRequest request);
}