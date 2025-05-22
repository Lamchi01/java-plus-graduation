package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.service.UserService;
import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.dto.UserShortDto;
import ru.yandex.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll(@RequestParam(required = false) List<Long> ids,
                                @RequestParam(defaultValue = "0") int from,
                                @RequestParam(defaultValue = "10") int size) {
        return userService.getAll(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        return userService.create(userDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("userId") long id) {
        userService.delete(id);
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{userId}/short")
    public UserShortDto getUserShortById(@PathVariable Long userId) {
        return userService.getUserShortById(userId);
    }

    @GetMapping("/list")
    public List<UserShortDto> getAllUsersShort(@RequestParam List<Long> ids) {
        return userService.getAllUsersShort(ids);
    }
}