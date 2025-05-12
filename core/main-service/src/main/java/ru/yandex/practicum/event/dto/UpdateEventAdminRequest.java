package ru.yandex.practicum.event.dto;

import ru.yandex.practicum.event.model.AdminStateAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest extends UpdateEventBaseRequest {
    private AdminStateAction stateAction;
}