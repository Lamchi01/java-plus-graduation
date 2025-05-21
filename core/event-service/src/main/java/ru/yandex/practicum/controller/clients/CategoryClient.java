package ru.yandex.practicum.controller.clients;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.feign.CategoryServiceClient;

@FeignClient(name = "category-service", path = "/categories")
public interface CategoryClient extends CategoryServiceClient {
}