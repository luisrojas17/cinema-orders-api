package com.cinema.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Common error envelope returned by every failing request.
 *
 * @param code    stable, machine-readable identifier (e.g. ORDER_NOT_FOUND)
 * @param message human-readable description
 * @param details field-level problems; omitted from the JSON when empty
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<ErrorDetail> details) {
}
