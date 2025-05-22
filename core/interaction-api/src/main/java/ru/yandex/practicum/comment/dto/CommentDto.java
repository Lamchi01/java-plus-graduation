package ru.yandex.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

//DTO Для вывода пользователю
@Getter
@Setter
public class CommentDto {
    private Long id;

    private Long eventId;

    private UserShortDto author;

    private String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
}