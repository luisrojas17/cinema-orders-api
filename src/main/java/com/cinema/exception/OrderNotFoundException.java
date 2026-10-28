package com.cinema.exception;

import com.cinema.exception.constant.ExceptionMessages;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId, String country) {
        super(ExceptionMessages.ORDER_NOT_FOUND.formatted(orderId, country));
    }
}
