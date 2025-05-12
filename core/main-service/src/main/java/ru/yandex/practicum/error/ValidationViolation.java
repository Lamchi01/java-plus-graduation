package ru.yandex.practicum.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ValidationViolation {
    private final String fieldName;

    private final String message;
}