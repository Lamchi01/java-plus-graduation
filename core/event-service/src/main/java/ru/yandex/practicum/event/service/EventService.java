package ru.yandex.practicum.event.service;

import ru.yandex.practicum.event.dto.*;
import ru.yandex.practicum.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventService {
    List<EventShortDto> getAllEvents(ReqParam reqParam);

    List<EventFullDto> getAllEvents(AdminEventParams params);

    EventFullDto publicGetEvent(long id);

    EventFullDto create(Long userId, NewEventDto newEventDto);

    List<EventShortDto> findUserEvents(Long userId, Integer from, Integer size);

    EventFullDto findUserEventById(Long userId, Long eventId);

    Optional<Event> getEventByIdAndInitiatorId(Long eventId, Long initiatorId);

    Optional<Event> getEventById(Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<Event> getAllEventByInitiatorId(Long initiatorId);

    List<Event> getAllEventsByIdIsIn(List<Long> ids);

    Boolean existsByCategoryId(Long catId);
}