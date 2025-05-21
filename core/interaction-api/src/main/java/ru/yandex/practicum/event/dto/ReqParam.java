package ru.yandex.practicum.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.event.model.EventSort;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@Getter
@Setter
public class ReqParam {
    private String text;

    private List<Long> categories;

    private Boolean paid;

    private LocalDateTime rangeStart;

    private LocalDateTime rangeEnd;

    private Boolean onlyAvailable;

    private EventSort sort;

    private int from;

    private int size;
}