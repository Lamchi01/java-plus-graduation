package ru.yandex.practicum.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(Class<?> entityClass, String message) {
        super(entityClass.getSimpleName() + message);
    }
}