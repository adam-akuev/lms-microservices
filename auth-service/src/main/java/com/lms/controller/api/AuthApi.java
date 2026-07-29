package com.lms.controller.api;

import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Аутентификация", description = "Управление регистрацией пользователей и входом в систему")
public interface AuthApi {

    @Operation(
        summary = "Регистрация нового пользователя",
        description = "Создает новую учетную запись пользователя с ролью STUDENT или TEACHER"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Пользователь успешно зарегистрирован"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Ошибка валидации входных данных (например, неверный формат email или слишком короткий пароль)"
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Пользователь с таким email уже существует в системе"
        )
    })
    @PostMapping("/register")
    ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request);

    @Operation(
        summary = "Авторизация в системе",
        description = "Проверяет email и пароль. В случае успеха возвращает JWT-токен доступа"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Успешная авторизация, возвращен Bearer JWT-токен",
            content = @Content(mediaType = "text/plain", schema = @Schema(example = "eyJhbGciOiJIUzI1NiJ9..."))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Некорректный формат запроса"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Неверный email или пароль"
        )
    })
    @PostMapping("/login")
    ResponseEntity<String> login(@Valid @RequestBody LoginRequest request);
}