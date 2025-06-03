package ru.practicum.ewm.event.clients;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.feign.RequestServiceClient;

@FeignClient(name = "request-service", path = "/users")
public interface RequestsClient extends RequestServiceClient {
}