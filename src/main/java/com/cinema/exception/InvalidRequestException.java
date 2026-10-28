package com.cinema.exception;

/** The request is syntactically valid but missing required content (maps to HTTP 400). */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
