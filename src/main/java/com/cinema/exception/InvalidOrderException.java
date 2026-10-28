package com.cinema.exception;

/** Business-rule violation on the content of an order (not a syntactic validation error). */
public class InvalidOrderException extends RuntimeException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
