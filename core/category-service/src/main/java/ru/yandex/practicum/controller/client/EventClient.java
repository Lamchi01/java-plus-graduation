package ru.yandex.practicum.controller.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.feign.EventServiceClient;

@FeignClient(name = "event-service")
public interface EventClient extends EventServiceClient {
}