package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserShortDto {
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 256)
    private String name;
}