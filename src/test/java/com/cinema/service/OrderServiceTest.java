package com.cinema.service;

import com.cinema.model.request.CreateOrderRequest;
import com.cinema.model.request.ItemRequest;
import com.cinema.model.request.UpdateOrderRequest;
import com.cinema.exception.InvalidOrderException;
import com.cinema.exception.InvalidRequestException;
import com.cinema.exception.OrderNotDeletableException;
import com.cinema.exception.OrderNotFoundException;
import com.cinema.model.ItemType;
import com.cinema.model.Order;
import com.cinema.model.OrderStatus;
import com.cinema.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(new OrderRepository());
    }

    private static ItemRequest ticket(String price) {
        return new ItemRequest(ItemType.TICKET, "Adulto", new BigDecimal(price));
    }

    private static ItemRequest food(String price) {
        return new ItemRequest(ItemType.FOOD, "Combo 1", new BigDecimal(price));
    }

    private Order createDefault() {
        return service.createOrder("mx", new CreateOrderRequest(472, "user-123", List.of(ticket("85"), food("120"))));
    }

    @Test
    void createAssignsUuidPendingStatusTimestampAndTotal() {
        Order order = createDefault();

        assertDoesNotThrow(() -> UUID.fromString(order.getOrderId()));
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getCreatedAt());
        assertEquals("MX", order.getCountry());
        assertEquals(new BigDecimal("205.00"), order.getTotal());
    }

    @Test
    void createRequiresAtLeastOneTicket() {
        var request = new CreateOrderRequest(472, "user-123", List.of(food("120")));
        assertThrows(InvalidOrderException.class, () -> service.createOrder("MX", request));
    }

    @Test
    void getFailsForUnknownOrderOrDifferentCountry() {
        Order order = createDefault();
        assertEquals(order.getOrderId(), service.getOrder("MX", order.getOrderId()).getOrderId());
        assertThrows(OrderNotFoundException.class, () -> service.getOrder("CO", order.getOrderId()));
        assertThrows(OrderNotFoundException.class, () -> service.getOrder("MX", UUID.randomUUID().toString()));
    }

    @Test
    void patchItemsRecalculatesTotalAndKeepsImmutableFields() {
        Order order = createDefault();

        Order updated = service.updateOrder("MX", order.getOrderId(),
                new UpdateOrderRequest(List.of(ticket("85.50")), null));

        assertEquals(new BigDecimal("85.50"), updated.getTotal());
        assertEquals(OrderStatus.PENDING, updated.getStatus());
        assertEquals(order.getCinemaId(), updated.getCinemaId());
        assertEquals(order.getUserId(), updated.getUserId());
        assertEquals(order.getCreatedAt(), updated.getCreatedAt());
    }

    @Test
    void patchStatusOnlyChangesStatus() {
        Order order = createDefault();
        Order updated = service.updateOrder("MX", order.getOrderId(), new UpdateOrderRequest(null, OrderStatus.CONFIRMED));
        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
        assertEquals(order.getTotal(), updated.getTotal());
    }

    @Test
    void patchWithoutFieldsIsRejected() {
        Order order = createDefault();
        assertThrows(InvalidRequestException.class,
                () -> service.updateOrder("MX", order.getOrderId(), new UpdateOrderRequest(null, null)));
    }

    @Test
    void deleteOnlyAllowedWhenPending() {
        Order pending = createDefault();
        service.deleteOrder("MX", pending.getOrderId());
        assertThrows(OrderNotFoundException.class, () -> service.getOrder("MX", pending.getOrderId()));

        Order confirmed = createDefault();
        service.updateOrder("MX", confirmed.getOrderId(), new UpdateOrderRequest(null, OrderStatus.CONFIRMED));
        assertThrows(OrderNotDeletableException.class, () -> service.deleteOrder("MX", confirmed.getOrderId()));
        assertDoesNotThrow(() -> service.getOrder("MX", confirmed.getOrderId()));
    }
}
