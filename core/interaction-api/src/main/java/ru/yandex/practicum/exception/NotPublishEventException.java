package ru.yandex.practicum.exception;

public class NotPublishEventException extends RuntimeException {
    public NotPublishEventException(String message) {
        super(message);
    }
}