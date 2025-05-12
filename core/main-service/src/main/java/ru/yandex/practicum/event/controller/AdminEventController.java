package ru.yandex.practicum.event.controller;

import ru.yandex.practicum.event.dto.AdminEventParams;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static ru.yandex.practicum.utility.Constants.FORMAT_DATETIME;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> adminGetAllEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        AdminEventParams adminEventParams = new AdminEventParams(users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getAllEvents(adminEventParams);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto adminUpdateEvent(@PathVariable("eventId") long eventId,
                                         @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        return eventService.update(eventId, updateEventAdminRequest);
    }
}