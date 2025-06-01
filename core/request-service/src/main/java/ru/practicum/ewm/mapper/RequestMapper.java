package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.requests.dto.ParticipationRequestDto;
import ru.practicum.ewm.requests.model.Request;

import static ru.practicum.ewm.utils.Constants.FORMAT_DATETIME;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(target = "event", source = "event")
    @Mapping(target = "requester", source = "requester")
    @Mapping(target = "created", source = "createdOn", dateFormat = FORMAT_DATETIME)
    ParticipationRequestDto toParticipationRequestDto(Request request);
}