package ru.yandex.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.dto.UpdateCommentDto;
import ru.yandex.practicum.comment.service.CommentService;
import ru.yandex.practicum.event.dto.EventCommentCount;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("commentId") long id) {
        commentService.adminDelete(id);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable("commentId") long id,
                             @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        return commentService.adminUpdate(id, updateCommentDto);
    }

    @GetMapping("/count")
    public List<EventCommentCount> getCountByEventIds(@RequestParam List<Long> eventsIds) {
        return commentService.findAllByEventIds(eventsIds);
    }

    @GetMapping("/count/{eventId}")
    public Long getCountByEventId(@PathVariable long eventId) {
        return commentService.getCountCommentByEvent_Id(eventId);
    }
}