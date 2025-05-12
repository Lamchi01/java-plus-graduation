package ru.yandex.practicum.event.controller;

import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.NewEventDto;
import ru.yandex.practicum.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {
    private final EventService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public EventFullDto create(@PathVariable(name = "userId") Long userId,
                               @Valid @RequestBody NewEventDto newEventDto) {
        return service.create(userId, newEventDto);
    }

    @GetMapping
    public Collection<EventShortDto> findUserEvents(
            @PathVariable(name = "userId") Long userId,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        return service.findUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findUserEventById(@PathVariable(name = "userId") Long userId,
                                          @PathVariable(name = "eventId") Long eventId) {
        return service.findUserEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUser(@PathVariable(name = "userId") Long userId,
                                          @PathVariable(name = "eventId") Long eventId,
                                          @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        return service.updateEventByUser(userId, eventId, updateRequest);
    }
}