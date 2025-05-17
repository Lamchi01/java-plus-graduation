package ru.yandex.practicum.event.controller;

import org.springframework.beans.factory.annotation.Value;
import ru.yandex.practicum.ParamHitDto;
import ru.yandex.practicum.client.RestStatClient;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.ReqParam;
import ru.yandex.practicum.event.model.EventSort;
import ru.yandex.practicum.event.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static ru.yandex.practicum.utility.Constants.FORMAT_DATETIME;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    @Value("${main.service.name}")
    private String MAIN_SERVICE;
    private final EventService eventService;
    private final RestStatClient statClient;

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
        List<EventShortDto> events = eventService.getAllEvents(reqParam);
        hit(request);
        return events;
    }

    @GetMapping("/{id}")
    public EventFullDto publicGetEvent(@PathVariable long id,
                                       HttpServletRequest request) {
        EventFullDto eventFullDto = eventService.publicGetEvent(id);
        hit(request);
        return eventFullDto;
    }

    private void hit(HttpServletRequest request) {
        ParamHitDto endpointHitDto = new ParamHitDto();
        endpointHitDto.setApp(MAIN_SERVICE);
        endpointHitDto.setUri(request.getRequestURI());
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now());
        statClient.hit(endpointHitDto);
    }
}