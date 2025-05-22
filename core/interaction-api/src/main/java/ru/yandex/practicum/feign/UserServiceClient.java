package ru.yandex.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.user.dto.UserShortDto;
import ru.yandex.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserServiceClient {
    @GetMapping("/{id}")
    Optional<User> getUserById(@PathVariable Long id) throws FeignException;

    @GetMapping("/{userId}/short")
    UserShortDto getUserShortById(@PathVariable Long userId) throws FeignException;

    @GetMapping("/list")
    List<UserShortDto> getAllUsersShort(@RequestParam List<Long> ids) throws FeignException;
}