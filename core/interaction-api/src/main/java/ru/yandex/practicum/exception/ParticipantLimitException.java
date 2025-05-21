package ru.yandex.practicum.exception;

public class ParticipantLimitException extends RuntimeException {
    public ParticipantLimitException(String message) {
        super(message);
    }
}