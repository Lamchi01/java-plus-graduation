package ru.practicum.ewm.controller.clients;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.feign.EventServiceClient;

@FeignClient(name = "event-service")
public interface EventClient extends EventServiceClient {
}