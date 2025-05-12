package ru.yandex.practicum.requests.dto;

import ru.yandex.practicum.requests.model.RequestStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    private RequestStatus status;
}