package ru.yandex.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.compilation.dto.CompilationDto;
import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.event.dto.EventShortDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
   @Mapping(target = "events", source = "events")
   CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events);
}
