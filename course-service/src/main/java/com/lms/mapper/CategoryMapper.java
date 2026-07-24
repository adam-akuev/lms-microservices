package com.lms.mapper;

import com.lms.dto.category.CategoryRequest;
import com.lms.dto.category.CategoryResponse;
import com.lms.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    Category toEntity(CategoryRequest request);
}