package com.cinema.model.request;

import com.cinema.model.ItemType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemRequest(
        @NotNull ItemType type,
        @NotBlank String name,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price) {
}
