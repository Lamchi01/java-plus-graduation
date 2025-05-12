package ru.yandex.practicum.compilation.mapper;

import ru.yandex.practicum.compilation.dto.CompilationDto;
import ru.yandex.practicum.compilation.model.Compilation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
   Compilation toCompilation(CompilationDto compilationDto);

   CompilationDto toCompilationDto(Compilation compilation);

   List<CompilationDto> toCompilationDtos(List<Compilation> compilations);
}
