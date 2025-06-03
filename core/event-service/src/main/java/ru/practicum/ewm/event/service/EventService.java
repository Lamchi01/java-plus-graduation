package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.*;

import java.util.List;

public interface EventService {
    List<EventShortDto> getAllEvents(ReqParam reqParam);

    List<EventFullDto> getAllEvents(AdminEventParams params);

    EventFullDto publicGetEvent(long id);

    EventFullDto create(Long userId, NewEventDto newEventDto);

    List<EventShortDto> findUserEvents(Long userId, Integer from, Integer size);

    EventFullDto findUserEventById(Long userId, Long eventId);

    EventFullDto getEventByIdAndInitiatorId(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventFullDto> getAllEventByInitiatorId(Long initiatorId);

    EventFullDto getEventById(Long eventId);

    List<EventShortDto> getRecommendationEvents(Long userId, Integer maxResult);

    EventShortDto likeEvent(Long userId, Long eventId);
}