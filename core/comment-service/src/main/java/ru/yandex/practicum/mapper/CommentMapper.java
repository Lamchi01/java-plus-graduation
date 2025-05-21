package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.dto.InputCommentDto;
import ru.yandex.practicum.comment.model.Comment;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", source = "author")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "created", ignore = true)
    Comment toComment(InputCommentDto inputCommentDto, User author, Event event);

    @Mapping(target = "eventId", source = "event.id")
    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", source = "admin")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    Comment toComment(CommentDto commentDto, User admin, Event event);

    @Mapping(target = "eventId", source = "event.id")
    List<CommentDto> toCommentDtos(List<Comment> comments);
}