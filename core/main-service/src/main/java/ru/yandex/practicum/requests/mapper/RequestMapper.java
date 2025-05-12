package ru.yandex.practicum.requests.mapper;

import ru.yandex.practicum.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.requests.model.Request;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static ru.yandex.practicum.utility.Constants.FORMAT_DATETIME;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "created", source = "createdOn", dateFormat = FORMAT_DATETIME)
    ParticipationRequestDto toParticipationRequestDto(Request request);
}