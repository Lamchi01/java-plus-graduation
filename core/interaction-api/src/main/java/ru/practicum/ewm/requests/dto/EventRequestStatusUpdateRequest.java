package ru.practicum.ewm.requests.dto;

import lombok.Getter;
import ru.practicum.ewm.requests.model.RequestStatus;

import java.util.List;

@Getter
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    private RequestStatus status;
}