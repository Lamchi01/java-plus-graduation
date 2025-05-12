package ru.yandex.practicum.user.service;

import ru.yandex.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    UserDto create(UserDto userDto);

    void delete(Long id);
}