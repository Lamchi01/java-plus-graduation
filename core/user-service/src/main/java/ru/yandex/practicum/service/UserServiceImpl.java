package ru.yandex.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exception.EntityNotFoundException;
import ru.yandex.practicum.mapper.UserMapper;
import ru.yandex.practicum.repository.UserRepository;
import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

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
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}