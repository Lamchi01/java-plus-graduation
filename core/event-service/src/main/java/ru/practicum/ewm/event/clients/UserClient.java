package ru.practicum.ewm.event.clients;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.feign.UserServiceClient;

@FeignClient(value = "user-service", path = "/admin/users")
public interface UserClient extends UserServiceClient {
}