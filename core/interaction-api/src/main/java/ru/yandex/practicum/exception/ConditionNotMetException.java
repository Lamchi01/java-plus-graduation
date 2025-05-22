package ru.yandex.practicum.exception;

public class ConditionNotMetException extends RuntimeException {
    public ConditionNotMetException(String message) {
        super(message);
    }
}