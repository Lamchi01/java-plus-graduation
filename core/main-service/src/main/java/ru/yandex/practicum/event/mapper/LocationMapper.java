package ru.yandex.practicum.event.mapper;

import ru.yandex.practicum.event.dto.LocationDto;
import ru.yandex.practicum.event.model.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location toLocation(LocationDto locationDto);

    LocationDto toLocationDto(Location location);
}