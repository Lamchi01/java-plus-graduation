package ewm.comment.controller;

import ewm.comment.dto.CommentDto;
import ewm.comment.dto.InputCommentDto;
import ewm.comment.dto.UpdateCommentDto;
import ewm.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{eventId}/{userId}")
    public CommentDto addComment(@PathVariable(name = "eventId") Long eventId,
                                 @PathVariable(name = "userId") Long userId,
                                 @Valid @RequestBody InputCommentDto inputCommentDto) {
        return commentService.privateAdd(userId, eventId, inputCommentDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{commentId}/{userId}")
    public void deleteComment(@PathVariable(name = "commentId") Long commentId,
                              @PathVariable(name = "userId") Long userId) {
        commentService.privateDelete(userId, commentId);
    }

    @PatchMapping("/{commentId}/{userId}")
    public CommentDto updateComment(@PathVariable(name = "commentId") Long commentId,
                                    @PathVariable(name = "userId") Long userId,
                                    @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        return commentService.privateUpdate(userId, commentId, updateCommentDto);
    }

    @GetMapping("/{eventId}/{userId}")
    public List<CommentDto> findCommentsByEventIdAndUserId(@PathVariable(name = "eventId") Long eventId,
                                                           @PathVariable(name = "userId") Long userId,
                                                           @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                           @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return commentService.findCommentsByEventIdAndUserId(eventId, userId, from, size);
    }

    @GetMapping("/users/{userId}")
    public List<CommentDto> findCommentsByUserId(@PathVariable(name = "userId") Long userId,
                                                 @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                 @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return commentService.findCommentsByUserId(userId, from, size);
    }
}