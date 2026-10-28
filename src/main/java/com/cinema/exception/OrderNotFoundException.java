package com.cinema.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId, String country) {
        super(ExceptionMessages.ORDER_NOT_FOUND.formatted(orderId, country));
    }
}
