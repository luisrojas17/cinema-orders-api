package com.cinema.exception;

import com.cinema.model.OrderStatus;

public class OrderNotDeletableException extends RuntimeException {
    public OrderNotDeletableException(String orderId, OrderStatus status) {
        super("Order '%s' cannot be deleted because its status is %s; only PENDING orders can be deleted"
                .formatted(orderId, status));
    }
}
