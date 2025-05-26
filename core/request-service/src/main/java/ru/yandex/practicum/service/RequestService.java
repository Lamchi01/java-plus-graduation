package ru.yandex.practicum.service;

import ru.yandex.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequest);

    List<ParticipationRequestDto> findAllByEventIdIn(List<Long> eventIds);
}