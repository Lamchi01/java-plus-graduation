package ru.practicum.ewm.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class EventFullDto extends EventBaseDto {
    private String createdOn;

    private String description;

    private String eventDate;

    private LocationDto location;

    private Long participantLimit;

    private String publishedOn;

    private Boolean requestModeration;

    private String state;
}