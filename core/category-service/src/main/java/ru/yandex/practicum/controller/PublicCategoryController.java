package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.service.CategoryService;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/categories")
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAll(@RequestParam(defaultValue = "0") Integer from,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return categoryService.getAll(from, size);
    }

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @GetMapping("{catId}/full")
    public Optional<Category> getFullCategoriesById(@PathVariable Long catId) {
        return categoryService.getFullCategoryById(catId);
    }
}