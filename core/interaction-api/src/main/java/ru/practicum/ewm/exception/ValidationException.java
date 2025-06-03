package ru.practicum.ewm.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(Class<?> entityClass, String message) {
        super(entityClass.getSimpleName() + message);
    }
}