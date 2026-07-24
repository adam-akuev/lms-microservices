package com.lms.service;

import com.lms.dto.category.CategoryRequest;
import com.lms.dto.category.CategoryResponse;
import com.lms.mapper.CategoryMapper;
import com.lms.model.Category;
import com.lms.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> getAll() {
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String trimmedName = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("Категория с таким названием уже существует");
        }

        Category category = categoryMapper.toEntity(request);

        category.setName(trimmedName);
        category.setSlug(generateSlug(trimmedName));

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    private String generateSlug(String input) {
        if (input == null || input.isBlank()) return "";
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\w-]", "").toLowerCase(Locale.ENGLISH);
    }
}