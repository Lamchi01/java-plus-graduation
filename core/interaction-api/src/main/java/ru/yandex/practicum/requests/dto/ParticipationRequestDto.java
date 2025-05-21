package ru.yandex.practicum.requests.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParticipationRequestDto {
    private String created;

    private Long event;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Long requester;

    private String status;
}