package ru.practicum.ewm.exception.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exception.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandlerControllerAdvice {
//  перехват эксепшенов валидации
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse onConstraintValidationException(
            ConstraintViolationException e
    ) {
        final List<ValidationViolation> validationViolations = e.getConstraintViolations().stream()
                .map(
                        violation -> {
                            log.error("ConstraintViolationException: {} : {}", violation.getPropertyPath().toString(), violation.getMessage());
                            return new ValidationViolation(
                                    violation.getPropertyPath().toString(),
                                    violation.getMessage()
                            );
                        }
                )
                .collect(Collectors.toList());

        return new ValidationErrorResponse(validationViolations);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse onMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        final List<ValidationViolation> validationViolations = e.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                            log.error("MethodArgumentNotValidException: {} : {}", error.getField(), error.getDefaultMessage());
                            return new ValidationViolation(error.getField(), error.getDefaultMessage());
                        }
                )
                .collect(Collectors.toList());

        return new ValidationErrorResponse(validationViolations);
    }


    @ExceptionHandler({EntityNotFoundException.class, OperationUnnecessaryException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError onEntityNotFoundException(final EntityNotFoundException e) {
        log.error("EntityNotFoundException - 404: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError("NOT_FOUND", "entity not found", stackTrace, LocalDateTime.now().toString());
    }

    @ExceptionHandler({EntityUpdateException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError onEntityUpdateException(final EntityUpdateException e) {
        log.error("EntityUpdateException - 409: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError("FORBIDDEN", "entity update forbidden", stackTrace, LocalDateTime.now().toString());
    }

    @ExceptionHandler({ConditionNotMetException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError onConditionNotMetException(final ConditionNotMetException e) {
        log.error("ConditionNotMetException - 409: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError(
                "CONFLICT",
                "For the requested operation the conditions are not met.",
                stackTrace,
                LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError onDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException - 409: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError("CONFLICT", "Integrity constraint has been violated", stackTrace, LocalDateTime.now().toString());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleAnyException(final Throwable e) {
        log.error("Error:500; {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();

        return new ApiError("INTERNAL_SERVER_ERROR", "internal server error", stackTrace, LocalDateTime.now().toString());
    }

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validationException(final ValidationException e) {
        log.error("ValidationException - 400: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError("BAD_REQUEST", "validation exception", stackTrace, LocalDateTime.now().toString());
    }

    @ExceptionHandler({NotPublishEventException.class,
            InitiatorRequestException.class,
            ParticipantLimitException.class,
            RepeatUserRequestorException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError onNotPublishEventException(final RuntimeException e) {
        log.error("409: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError("CONFLICT", "event is not published", stackTrace, LocalDateTime.now().toString());
    }

    public record ApiError(String status, String reason, String message, String timestamp) {
    }
}