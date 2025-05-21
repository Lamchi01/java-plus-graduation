package ru.yandex.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventServiceClient {
    @GetMapping("/admin/events/existsbycategory/{catId}")
    Boolean existsByCategory(@PathVariable Long catId) throws FeignException;

    @GetMapping("/admin/events/{eventId}/full")
    Optional<Event> getEventFullById(@PathVariable long eventId) throws FeignException;

    @PostMapping("/admin/events")
    Event saveEvent(@RequestBody Event event) throws FeignException;

    @GetMapping("/users/{userId}/events/{eventId}/optional")
    Optional<Event> getEventByIdAndInitiatorId(@PathVariable long userId, @PathVariable long eventId) throws FeignException;

    @GetMapping("/users/{userId}/events/initiator")
    List<Event> getAllEventByInitiatorId(@PathVariable long userId) throws FeignException;

    @GetMapping("/admin/events/all")
    List<Event> getAllEventsByIdIsIn(@RequestParam List<Long> eventIds) throws FeignException;
}