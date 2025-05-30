package ru.yandex.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.util.List;

public interface UserServiceClient {
    @GetMapping("/{userId}/short")
    UserShortDto getUserShortById(@PathVariable Long userId) throws FeignException;

    @GetMapping("/list")
    List<UserShortDto> getAllUsersShort(@RequestParam List<Long> ids) throws FeignException;
}