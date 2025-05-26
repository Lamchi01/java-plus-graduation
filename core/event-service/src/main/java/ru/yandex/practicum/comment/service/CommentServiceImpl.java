package ru.yandex.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.dto.InputCommentDto;
import ru.yandex.practicum.comment.dto.UpdateCommentDto;
import ru.yandex.practicum.comment.mapper.CommentMapper;
import ru.yandex.practicum.comment.model.Comment;
import ru.yandex.practicum.comment.repository.CommentRepository;
import ru.yandex.practicum.event.clients.RequestsClient;
import ru.yandex.practicum.event.clients.UserClient;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.EntityNotFoundException;
import ru.yandex.practicum.exception.InitiatorRequestException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final RequestsClient requestsClient;

    @Override
    public CommentDto privateAdd(Long userId, Long eventId, InputCommentDto inputCommentDto) {
        Event event = findEvent(eventId);
        if (event.getInitiator().equals(userId)) {
            throw new ValidationException(Comment.class, " Нельзя оставлять комментарии к своему событию.");
        }
        if (requestsClient.getRequestsForUserEvent(userId, eventId).isEmpty()) {
            throw new ValidationException(Comment.class, " Пользователь с ID - " + userId + ", не заявился на событие с ID - " + eventId + ".");
        }
        UserShortDto author = findShortUser(userId);

        Comment comment = commentMapper.toComment(inputCommentDto, author, event);
        comment.setCreated(LocalDateTime.now());
        return commentMapper.toCommentDto(commentRepository.save(comment), findShortUser(userId));
    }

    @Override
    public void privateDelete(Long userId, Long commentId) {
        UserShortDto author = findShortUser(userId);
        Comment comment = findComment(commentId);
        if (!comment.getAuthor().equals(userId)) {
            throw new InitiatorRequestException(" Нельзя удалить комментарий другого пользователя.");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public void adminDelete(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public CommentDto privateUpdate(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        UserShortDto author = findShortUser(userId);
        Comment comment = findComment(commentId);
        if (!comment.getAuthor().equals(userId)) {
            throw new InitiatorRequestException(" Нельзя редактировать комментарий другого пользователя.");
        }
        comment.setText(updateCommentDto.getText());
        return commentMapper.toCommentDto(commentRepository.save(comment), author);
    }

    @Override
    public CommentDto adminUpdate(Long id, UpdateCommentDto updateCommentDto) {

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, "Комментарий c ID - " + id + ", не найден."));

        if (updateCommentDto.getText() != null) {
            comment.setText(updateCommentDto.getText());
        }
        return commentMapper.toCommentDto(commentRepository.save(comment), findShortUser(comment.getAuthor()));
    }

    @Override
    public List<CommentDto> findCommentsByEventId(Long eventId, Integer from, Integer size) {
        findEvent(eventId);
        Pageable pageable = PageRequest.of(from, size);
        List<Comment> comments = commentRepository.findAllByEventId(eventId, pageable);
        return comments.stream().map(comment -> commentMapper.toCommentDto(comment, findShortUser(comment.getAuthor()))).toList();
    }

    @Override
    public CommentDto findCommentById(Long commentId) {
        Comment comment = findComment(commentId);
        return commentMapper.toCommentDto(comment, findShortUser(comment.getAuthor()));
    }

    @Override
    public List<CommentDto> findCommentsByEventIdAndUserId(Long eventId, Long userId, Integer from, Integer size) {
        UserShortDto user = findShortUser(userId);
        Event event = findEvent(eventId);
        Pageable pageable = PageRequest.of(from, size);
        List<Comment> comments = commentRepository.findAllByEventIdAndAuthor(eventId, userId, pageable);
        UserShortDto userShortDto = findShortUser(user.getId());
        return comments.stream().map(comment -> commentMapper.toCommentDto(comment, userShortDto)).toList();
    }

    @Override
    public List<CommentDto> findCommentsByUserId(Long eventId, Integer from, Integer size) {
        UserShortDto user = findShortUser(eventId);
        Pageable pageable = PageRequest.of(from, size);
        List<Comment> comments = commentRepository.findAllByAuthor(user.getId(), pageable);
        UserShortDto userShortDto = findShortUser(user.getId());
        return comments.stream()
                .map(comment -> commentMapper.toCommentDto(comment, userShortDto))
                .toList();
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(
                        Event.class, "Событие c ID - " + eventId + ", не найдено или ещё не опубликовано")
                );
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, "Комментарий c ID - " + commentId + ", не найден."));
    }

    private UserShortDto findShortUser(Long userId) {
        return userClient.getUserShortById(userId);
    }
}