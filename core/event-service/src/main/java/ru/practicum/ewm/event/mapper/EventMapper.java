package ru.practicum.ewm.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.requests.dto.ParticipationRequestDto;
import ru.practicum.ewm.stats.protobuf.RecommendedEventProto;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewm.utils.Constants.FORMAT_DATETIME;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {
    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "rating", ignore = true)
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

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "initiator", source = "initiator")
    EventShortDto mapToShortDto(Event event, Double rating, UserShortDto initiator);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "initiator", source = "initiator")
    EventFullDto mapToFullDto(Event event, Double rating, UserShortDto initiator);

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

    default List<EventShortDto> mapToShortDto(List<Event> events, List<UserShortDto> initiators,
                                              List<RecommendedEventProto> ratingList) {
        Map<Long, UserShortDto> initiatorsMap = initiators.stream()
                .collect(Collectors.toMap(UserShortDto::getId, e -> e, (existing, replacement) -> existing));

        return mapToShortDto(events, initiatorsMap, ratingList);
    }

    default List<EventShortDto> mapToShortDto(List<Event> events, Map<Long, UserShortDto> initiatorsMap,
                                              List<RecommendedEventProto> ratingList) {
        return events.stream().map(event -> {
            Long eventId = event.getId();
            Optional<RecommendedEventProto> rating = ratingList.stream()
                    .filter(recommendedEvent -> recommendedEvent.getEventId() == eventId)
                    .findFirst();

            UserShortDto initiator = initiatorsMap.get(event.getInitiator());

            return mapToShortDto(
                    event,
                    rating.map(RecommendedEventProto::getScore).orElse(0.0),
                    initiator
            );
        }).collect(Collectors.toList());
    }

    default List<EventFullDto> mapToFullDto(List<Event> events, List<UserShortDto> initiators,
                                            List<RecommendedEventProto> ratingList,
                                            List<ParticipationRequestDto> confirmedRequests) {
        Map<Long, Integer> confirmedRequestsCountMap = confirmedRequests.stream()
                .collect(Collectors.groupingBy(ParticipationRequestDto::getEvent, Collectors.summingInt(e -> 1)));

        Map<Long, UserShortDto> initiatorsMap = initiators.stream()
                .collect(Collectors.toMap(UserShortDto::getId, e -> e));

        return events.stream().map(event -> {
            Long eventId = event.getId();
            Optional<RecommendedEventProto> rating = ratingList.stream()
                    .filter(recommendedEvent -> recommendedEvent.getEventId() == eventId)
                    .findFirst();

            UserShortDto initiator = initiatorsMap.get(event.getInitiator());

            return mapToFullDto(
                    event,
                    rating.map(RecommendedEventProto::getScore).orElse(0.0),
                    initiator
            );
        }).collect(Collectors.toList());
    }
}