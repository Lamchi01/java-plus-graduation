package ru.yandex.practicum.comment.service;

import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.dto.InputCommentDto;
import ru.yandex.practicum.comment.dto.UpdateCommentDto;
import ru.yandex.practicum.comment.mapper.CommentMapper;
import ru.yandex.practicum.comment.model.Comment;
import ru.yandex.practicum.comment.repository.CommentRepository;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.EventState;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.EntityNotFoundException;
import ru.yandex.practicum.exception.InitiatorRequestException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.requests.repository.RequestRepository;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentDto privateAdd(Long userId, Long eventId, InputCommentDto inputCommentDto) {
        Event event = findEvent(eventId);
        if (event.getInitiator().getId().equals(userId)) {
            throw new ValidationException(Comment.class, " Нельзя оставлять комментарии к своему событию.");
        }
        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isEmpty()) {
            throw new ValidationException(Comment.class, " Пользователь с ID - " + userId + ", не заявился на событие с ID - " + eventId + ".");
        }
        User author = findUser(userId);

        Comment comment = commentMapper.toComment(inputCommentDto, author, event);
        comment.setCreated(LocalDateTime.now());
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void privateDelete(Long userId, Long commentId) {
        User author = findUser(userId);
        Comment comment = findComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
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
        User author = findUser(userId);
        Comment comment = findComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new InitiatorRequestException(" Нельзя редактировать комментарий другого пользователя.");
        }
        comment.setText(updateCommentDto.getText());
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto adminUpdate(Long id, UpdateCommentDto updateCommentDto) {

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, "Комментарий c ID - " + id + ", не найден."));

        if (updateCommentDto.getText() != null) {
            comment.setText(updateCommentDto.getText());
        }
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> findCommentsByEventId(Long eventId, Integer from, Integer size) {
        findEvent(eventId);
        Pageable pageable = PageRequest.of(from, size);
        return commentMapper.toCommentDtos(commentRepository.findAllByEventId(eventId, pageable));
    }

    @Override
    public CommentDto findCommentById(Long commentId) {
        Comment comment = findComment(commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> findCommentsByEventIdAndUserId(Long eventId, Long userId, Integer from, Integer size) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        Pageable pageable = PageRequest.of(from, size);
        return commentMapper.toCommentDtos(commentRepository.findAllByEventIdAndAuthorId(eventId, userId, pageable));
    }

    @Override
    public List<CommentDto> findCommentsByUserId(Long eventId, Integer from, Integer size) {
        User user = findUser(eventId);
        Pageable pageable = PageRequest.of(from, size);
        return commentMapper.toCommentDtos(commentRepository.findAllByAuthorId(user.getId(), pageable));
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException(
                        Event.class, "Событие c ID - " + eventId + ", не найдено или ещё не опубликовано")
                );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Пользователь c ID - " + userId + ", не найден."));
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, "Комментарий c ID - " + commentId + ", не найден."));
    }
}