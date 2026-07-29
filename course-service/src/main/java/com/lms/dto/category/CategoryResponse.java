package com.lms.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Данные категории")
public class CategoryResponse {
    @Schema(description = "Идентификатор категории", example = "1")
    private Long id;

    @Schema(description = "Название категории", example = "Программирование")
    private String name;

    @Schema(description = "Человекопонятный URL (slug)", example = "programming")
    private String slug;
}