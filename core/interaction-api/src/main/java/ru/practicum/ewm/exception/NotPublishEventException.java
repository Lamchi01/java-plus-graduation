package ru.practicum.ewm.exception;

public class NotPublishEventException extends RuntimeException {
    public NotPublishEventException(String message) {
        super(message);
    }
}