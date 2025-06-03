package ru.practicum.ewm.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.EntityNotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);

        if (ids != null && !ids.isEmpty()) {
            return userMapper.toUserDtoList(userRepository.findByIdIn(ids, pageable));
        } else {
            return userMapper.toUserDtoList(userRepository.findAll(pageable).getContent());
        }
    }

    @Override
    public UserDto create(UserDto userDto) {
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(userDto)));
    }

    @Override
    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Пользователь c ID - " + id + ", не найден."));
        userRepository.deleteById(id);
    }

    @Override
    public UserShortDto getUserShortById(Long userIid) {
        return userMapper.toUserShortDto(userRepository.findById(userIid)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Пользователь c ID - " + userIid + ", не найден.")));
    }

    @Override
    public List<UserShortDto> getAllUsersShort(List<Long> userIds) {
        return userRepository.findByIdIn(userIds).stream()
                .map(userMapper::toUserShortDto)
                .toList();
    }
}