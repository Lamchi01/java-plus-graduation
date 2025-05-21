package ru.yandex.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.user.model.User;

import java.util.Optional;

public interface UserServiceClient {
    @GetMapping("/{id}")
    Optional<User> getUserById(@PathVariable Long id) throws FeignException;
}