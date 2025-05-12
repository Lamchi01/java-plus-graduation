package ewm.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import ewm.user.dto.UserShortDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

//DTO Для вывода пользователю
@Getter
@Setter
public class CommentDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Long eventId;

    private UserShortDto author;

    private String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
}