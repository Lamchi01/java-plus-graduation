package ru.yandex.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.NewEventDto;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.yandex.practicum.utils.Constants.FORMAT_DATETIME;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {
    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "initiator", source = "userShortDto")
    EventFullDto toEventFullDto(Event event, UserShortDto userShortDto);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "eventDate", source = "eventDate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "state", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "initiator", source = "userShortDto")
    EventShortDto toEventShortDto(Event event, UserShortDto userShortDto, CategoryDto categoryDto);

    List<EventShortDto> toEventShortDtos(List<EventFullDto> eventFullDtos);

    default LocalDateTime stringToLocalDateTime(String stringDate) {
        if (stringDate == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATETIME);
        return LocalDateTime.parse(stringDate, formatter);
    }

    default String localDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATETIME);
        return localDateTime.format(formatter);
    }
}