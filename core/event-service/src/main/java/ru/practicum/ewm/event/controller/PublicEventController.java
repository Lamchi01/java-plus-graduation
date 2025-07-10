package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.ReqParam;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.utils.Constants.FORMAT_DATETIME;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;
    private static final String headerUserId = "X-EWM-USER-ID";

    @GetMapping
    public List<EventShortDto> publicGetAllEvents(@RequestParam(required = false) String text,
                                                  @RequestParam(required = false) List<Long> categories,
                                                  @RequestParam(required = false) Boolean paid,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime rangeStart,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime rangeEnd,
                                                  @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                  @RequestParam(required = false) EventSort sort,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  HttpServletRequest request) {
        ReqParam reqParam = ReqParam.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();
        return eventService.getAllEvents(reqParam);
    }

    @GetMapping("/{id}")
    public EventFullDto publicGetEvent(@PathVariable long id,
                                       HttpServletRequest request) {
        return eventService.publicGetEvent(id);
    }

    @GetMapping("/recommednations")
    public List<EventShortDto> getRecommendationEvents(@RequestHeader(headerUserId) Long userId,
                                                       @RequestParam(defaultValue = "10") Integer maxResult) {
        return eventService.getRecommendationEvents(userId, maxResult);
    }

    @PutMapping("{eventId}/like")
    public EventShortDto likeEvent(@RequestHeader(headerUserId) Long userId,
                                   @PathVariable Long eventId) {
        return eventService.likeEvent(userId, eventId);
    }
}