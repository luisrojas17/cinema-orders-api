package com.cinema.model.request;

import com.cinema.constant.ValidationMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Body of POST /v1/countries/{country}/orders.
 * Only "cinema_id", "user_id" and "items" are required. At least, one item is required.
 */
public record CreateOrderRequest(
        @NotNull
        @Positive(message = ValidationMessages.CINEMA_ID_MESSAGE)
        Integer cinemaId,
        @NotBlank String userId,
        @NotEmpty List<@NotNull @Valid ItemRequest> items) {
}
