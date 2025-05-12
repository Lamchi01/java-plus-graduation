package ewm.requests.dto;

import ewm.requests.model.RequestStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    private RequestStatus status;
}