package ru.yandex.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.category.model.Category;

import java.util.Optional;

public interface CategoryServiceClient {
    @GetMapping("/{catId}/full")
    Optional<Category> getFullCategoriesById(@PathVariable Long catId) throws FeignException;
}