package ru.yandex.practicum.comment.controller;

import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping(path = "/comments")
@RequiredArgsConstructor
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping("/events/{eventId}")
    public Collection<CommentDto> findCommentsByEventId(@PathVariable(name = "eventId") Long eventId,
                                                        @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                        @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return commentService.findCommentsByEventId(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto findCommentById(@PathVariable(name = "commentId") Long commentId) {
        return commentService.findCommentById(commentId);
    }
}