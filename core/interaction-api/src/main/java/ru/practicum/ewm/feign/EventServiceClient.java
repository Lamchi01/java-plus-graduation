package ru.practicum.ewm.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.event.dto.EventFullDto;

import java.util.List;

public interface EventServiceClient {
    @GetMapping("/admin/events/{eventId}/full")
    EventFullDto getEventFullById(@PathVariable long eventId) throws FeignException;

    @GetMapping("/users/{userId}/events/{eventId}/optional")
    EventFullDto getEventByIdAndInitiatorId(@PathVariable long userId, @PathVariable long eventId) throws FeignException;

    @GetMapping("/users/{userId}/events/initiator")
    List<EventFullDto> getAllEventByInitiatorId(@PathVariable long userId) throws FeignException;
}