package ru.practicum.ewm.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestServiceClient {
    @GetMapping("/{userId}/requests/{eventId}")
    List<ParticipationRequestDto> getRequestsForUserEvent(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;

    @GetMapping("/{userId}/events/requests/all")
    List<ParticipationRequestDto> findAllByEventIdIn(@PathVariable Long userId, @RequestParam List<Long> eventIds) throws FeignException;
}