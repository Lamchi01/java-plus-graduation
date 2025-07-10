package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

@Data
public class CompilationDto {
    private Long id;

    private List<EventShortDto> events;

    private Boolean pinned = false;

    @NotBlank(message = "Название подборки не может быть пустым")
    private String title;
}