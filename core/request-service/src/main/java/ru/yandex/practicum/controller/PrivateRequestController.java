package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.requests.model.Request;
import ru.yandex.practicum.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateRequestController {

    private final RequestService requestService;

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                                 @RequestParam Long eventId) {
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusRequest(@PathVariable Long userId, @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest eventRequest) {
        return requestService.updateStatusRequest(userId, eventId, eventRequest);
    }

    @GetMapping("/events/{eventId}/requests/confirmedcount")
    public Long getCountConfirmedRequestsByEventId(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.getCountConfirmedRequestsByEventId(eventId);
    }

    @GetMapping("/events/requests/all")
    public List<Request> findAllByEventIdIn(@PathVariable Long userId, @RequestParam List<Long> eventIds) {
        return requestService.findAllByEventIdIn(eventIds);
    }
}