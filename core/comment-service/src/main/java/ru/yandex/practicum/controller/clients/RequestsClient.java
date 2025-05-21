package ru.yandex.practicum.controller.clients;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.feign.RequestServiceClient;

@FeignClient(name = "requests-service", path = "/users")
public interface RequestsClient extends RequestServiceClient {
}