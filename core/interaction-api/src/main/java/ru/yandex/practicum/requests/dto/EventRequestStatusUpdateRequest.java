package ru.yandex.practicum.requests.dto;

import lombok.Getter;
import ru.yandex.practicum.requests.model.RequestStatus;

import java.util.List;

@Getter
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    private RequestStatus status;
}