package ru.yandex.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestServiceClient {
    @GetMapping("/{userId}/requests/{eventId}")
    List<ParticipationRequestDto> getRequestsForUserEvent(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;

    @GetMapping("/{userId}/events/requests/all")
    List<ParticipationRequestDto> findAllByEventIdIn(@PathVariable Long userId, @RequestParam List<Long> eventIds) throws FeignException;
}