package ru.yandex.practicum.event.clients;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.feign.RequestServiceClient;

@FeignClient(name = "request-service", path = "/users")
public interface RequestsClient extends RequestServiceClient {
}