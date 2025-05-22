package ru.yandex.practicum.category.service;

import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.dto.NewCategoryDto;
import ru.yandex.practicum.category.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getCategoryById(Long id);

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long id);

    CategoryDto updateCategory(Long id, CategoryDto categoryDto);

    Optional<Category> getFullCategoryById(Long id);
}