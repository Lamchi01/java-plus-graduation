package ru.yandex.practicum.exception;

public class InitiatorRequestException extends RuntimeException {
    public InitiatorRequestException(String message) {
        super(message);
    }
}