package com.cinema.controller;

import com.cinema.constant.ValidationMessages;
import com.cinema.model.request.CreateOrderRequest;
import com.cinema.model.request.UpdateOrderRequest;
import com.cinema.model.Order;
import com.cinema.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * POST   /v1/countries/{country}/orders           - Create an order (201)
 * GET    /v1/countries/{country}/orders/{orderId} - Get an order (200)
 * PATCH  /v1/countries/{country}/orders/{orderId} - Partially update an order (200)
 * DELETE /v1/countries/{country}/orders/{orderId} - Delete an order (204)
 *
 * Errors (see GlobalExceptionHandler): 400 format/null/empty validation, 404 order not found,
 * 422 business-rule violation, 500 unexpected error.
 */
@RestController
@RequestMapping("/v1/countries/{country}/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private static final String COUNTRY_REGEX = "^[A-Za-z]{2}$";
    private static final String UUID_REGEX =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";


    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @PathVariable @Pattern(regexp = COUNTRY_REGEX, message = ValidationMessages.COUNTRY_MESSAGE) String country,
            @Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(country, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{orderId}")
                .buildAndExpand(order.getOrderId())
                .toUri();
        return ResponseEntity.created(location).body(order);
    }

    @GetMapping("/{orderId}")
    public Order getOrder(
            @PathVariable @Pattern(regexp = COUNTRY_REGEX, message = ValidationMessages.COUNTRY_MESSAGE) String country,
            @PathVariable @Pattern(regexp = UUID_REGEX, message = ValidationMessages.UUID_MESSAGE) String orderId) {
        return orderService.getOrder(country, orderId);
    }

    @PatchMapping("/{orderId}")
    public Order updateOrder(
            @PathVariable @Pattern(regexp = COUNTRY_REGEX, message = ValidationMessages.COUNTRY_MESSAGE) String country,
            @PathVariable @Pattern(regexp = UUID_REGEX, message = ValidationMessages.UUID_MESSAGE) String orderId,
            @Valid @RequestBody UpdateOrderRequest request) {
        return orderService.updateOrder(country, orderId, request);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable @Pattern(regexp = COUNTRY_REGEX, message = ValidationMessages.COUNTRY_MESSAGE) String country,
            @PathVariable @Pattern(regexp = UUID_REGEX, message = ValidationMessages.UUID_MESSAGE) String orderId) {
        orderService.deleteOrder(country, orderId);
        return ResponseEntity.noContent().build();
    }
}
