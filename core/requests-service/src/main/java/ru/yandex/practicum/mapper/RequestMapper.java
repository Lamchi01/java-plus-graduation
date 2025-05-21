package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.requests.model.Request;

import static ru.yandex.practicum.utils.Constants.FORMAT_DATETIME;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "created", source = "createdOn", dateFormat = FORMAT_DATETIME)
    ParticipationRequestDto toParticipationRequestDto(Request request);
}