package com.cinema.exception;

public abstract class BusinessException extends RuntimeException {

    public BusinessException(final String message) {
        super(message);
    }

}
