package com.cinema.exception.model;

/** A single field-level problem inside an {@link ApiError}. */
public record ErrorDetail(String field, String message) {
}
