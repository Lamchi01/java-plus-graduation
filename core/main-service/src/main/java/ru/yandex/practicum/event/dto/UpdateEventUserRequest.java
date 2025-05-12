package ru.yandex.practicum.event.dto;

import ru.yandex.practicum.event.model.PrivateStateAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest extends UpdateEventBaseRequest {
    private PrivateStateAction stateAction;
}