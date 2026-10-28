package com.cinema.exception.handler;

import com.cinema.exception.model.ApiError;
import com.cinema.exception.model.ErrorDetail;
import com.cinema.exception.InvalidOrderException;
import com.cinema.exception.InvalidRequestException;
import com.cinema.exception.OrderNotDeletableException;
import com.cinema.exception.OrderNotFoundException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Translates every failure into the common {@link ApiError} envelope. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(OrderNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(OrderNotDeletableException.class)
    public ResponseEntity<ApiError> handleNotDeletable(OrderNotDeletableException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "ORDER_NOT_DELETABLE", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ApiError> handleInvalidOrder(InvalidOrderException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ORDER", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(InvalidRequestException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request, List.of());
    }

    /** Body validation (@Valid @RequestBody). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorDetail(toSnakeCase(fe.getField()), fe.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", request, details);
    }

    /** Path variable validation (@Validated controller). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ErrorDetail> details = ex.getConstraintViolations().stream()
                .map(v -> new ErrorDetail(toSnakeCase(lastNode(v)), v.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", request, details);
    }

    /** Malformed JSON, wrong types, or invalid enum values (e.g. an unknown status). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Malformed or missing JSON request body";
        if (ex.getCause() instanceof InvalidFormatException ife) {
            String field = jsonPath(ife.getPath());
            message = "Invalid value '%s' for field '%s'".formatted(ife.getValue(), field);
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                message += ". Allowed values: " + Arrays.toString(ife.getTargetType().getEnumConstants());
            }
        }
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", message, request, List.of());
    }

    /** Anything else; Spring's own HTTP errors (405, 415, ...) keep their status. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            String message = Optional.ofNullable(errorResponse.getBody().getDetail()).orElse(status.getReasonPhrase());
            return build(status, "HTTP_ERROR", message, request, List.of());
        }
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", request, List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message,
                                           HttpServletRequest request, List<ErrorDetail> details) {
        ApiError body = new ApiError(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                details);
        return ResponseEntity.status(status).body(body);
    }

    private String lastNode(ConstraintViolation<?> violation) {
        String name = "";
        for (Path.Node node : violation.getPropertyPath()) {
            name = node.getName();
        }
        return name;
    }

    private String jsonPath(List<JsonMappingException.Reference> path) {
        StringBuilder sb = new StringBuilder();
        for (JsonMappingException.Reference ref : path) {
            if (ref.getFieldName() != null) {
                if (!sb.isEmpty()) {
                    sb.append('.');
                }
                sb.append(toSnakeCase(ref.getFieldName()));
            } else if (ref.getIndex() >= 0) {
                sb.append('[').append(ref.getIndex()).append(']');
            }
        }
        return sb.toString();
    }

    private String toSnakeCase(String name) {
        return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}
