package ru.yandex.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.requests.model.Request;

import java.util.List;

public interface RequestServiceClient {
    @GetMapping("/{userId}/requests")
    List<ParticipationRequestDto> getRequestsOfUser(@PathVariable Long userId) throws FeignException;

    @PostMapping("/{userId}/requests")
    ParticipationRequestDto addRequest(@PathVariable Long userId, @RequestParam Long eventId) throws FeignException;

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) throws FeignException;

    @GetMapping("/{userId}/requests/{eventId}")
    List<ParticipationRequestDto> getRequestsForUserEvent(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;

    @PatchMapping("/{userId}/requests/{eventId}/change")
    EventRequestStatusUpdateResult changeRequestsStatus(@PathVariable Long userId,
                                                                        @PathVariable Long eventId,
                                                                        @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) throws FeignException;

    @GetMapping("/{userId}/events/{eventId}/requests/confirmedcount")
    Long getCountConfirmedRequestsByEventId(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;

    @GetMapping("/{userId}/events/requests/all")
    List<Request> findAllByEventIdIn(@PathVariable Long userId, @RequestParam List<Long> eventIds) throws FeignException;
}