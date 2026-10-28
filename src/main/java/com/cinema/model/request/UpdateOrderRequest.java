package com.cinema.model.request;

import com.cinema.constant.ValidationMessages;
import com.cinema.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Body of PATCH /v1/countries/{country}/orders/{orderId}.
 * Only "items" and "status" can be modified; both are optional but at least one is required.
 */
public record UpdateOrderRequest(
        @Size(min = 1, message = ValidationMessages.ITEMS_MESSAGE)
        List<@NotNull @Valid ItemRequest> items,
        OrderStatus status) {
}
