package ewm.compilation.mapper;

import ewm.compilation.dto.CompilationDto;
import ewm.compilation.model.Compilation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
   Compilation toCompilation(CompilationDto compilationDto);

   CompilationDto toCompilationDto(Compilation compilation);

   List<CompilationDto> toCompilationDtos(List<Compilation> compilations);
}
