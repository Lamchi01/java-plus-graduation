package ru.yandex.practicum.service;

import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    UserDto create(UserDto userDto);

    void delete(Long id);

    Optional<User> getUserById(Long id);
}