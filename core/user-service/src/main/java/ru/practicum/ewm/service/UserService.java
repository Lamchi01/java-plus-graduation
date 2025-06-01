package ru.practicum.ewm.service;

import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    UserDto create(UserDto userDto);

    void delete(Long id);

    UserShortDto getUserShortById(Long userIid);

    List<UserShortDto> getAllUsersShort(List<Long> userIds);
}