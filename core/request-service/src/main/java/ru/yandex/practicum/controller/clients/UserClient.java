package ru.yandex.practicum.controller.clients;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.feign.UserServiceClient;

@FeignClient(value = "user-service", path = "/admin/users")
public interface UserClient extends UserServiceClient {
}