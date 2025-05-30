package ru.yandex.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.user.dto.UserShortDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventBaseDto {
    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;

    private Long id;

    private UserShortDto initiator;

    private Boolean paid;

    private String title;

    private Long views;

    private Long commentsCount;
}