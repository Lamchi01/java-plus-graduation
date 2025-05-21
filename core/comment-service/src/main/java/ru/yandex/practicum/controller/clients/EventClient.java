package ru.yandex.practicum.controller.clients;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.feign.EventServiceClient;

@FeignClient(name = "event-service")
public interface EventClient extends EventServiceClient {
}