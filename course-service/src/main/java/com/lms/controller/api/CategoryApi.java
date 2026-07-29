package com.lms.controller.api;

import com.lms.dto.category.CategoryRequest;
import com.lms.dto.category.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Категории", description = "Управление категориями курсов")
@SecurityRequirement(name = "bearerAuth")
public interface CategoryApi {

    @Operation(
            summary = "Получить список всех категорий",
            description = "Возвращает полный список доступных категорий для курсов"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список категорий успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    ResponseEntity<List<CategoryResponse>> getAllCategories();

    @Operation(
            summary = "Создать новую категорию",
            description = "Создает новую категорию курсов (доступно преподавателям)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Категория успешно создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входящих данных"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль TEACHER)")
    })
    ResponseEntity<CategoryResponse> createCategory(CategoryRequest request);
}