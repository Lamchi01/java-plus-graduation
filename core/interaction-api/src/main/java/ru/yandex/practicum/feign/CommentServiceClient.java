package ru.yandex.practicum.feign;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.event.dto.EventCommentCount;

import java.util.List;

public interface CommentServiceClient {
    @GetMapping("/count")
    List<EventCommentCount> getCountByEventIds(@RequestParam List<Long> eventsIds);

    @GetMapping("/count/{eventId}")
    Long getCountByEventId(@PathVariable Long eventId);
}