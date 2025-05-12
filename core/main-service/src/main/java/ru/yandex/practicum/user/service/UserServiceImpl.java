package ru.yandex.practicum.user.service;

import ru.yandex.practicum.exception.EntityNotFoundException;
import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.mapper.UserMapper;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);

        if (ids != null && !ids.isEmpty()) {
            return userMapper.toUserDtoList(userRepository.findByIdIn(ids, pageable));
        } else {
            return userMapper.toUserDtoList(userRepository.findAll(pageable).getContent());
        }
    }

    public UserDto create(UserDto userDto) {
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(userDto)));
    }

    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Пользователь c ID - " + id + ", не найден."));
        userRepository.deleteById(id);
    }
}