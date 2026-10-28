package com.cinema.service;

import com.cinema.exception.*;
import com.cinema.exception.constant.ExceptionMessages;
import com.cinema.helper.OrderHelper;
import com.cinema.model.request.CreateOrderRequest;
import com.cinema.model.request.ItemRequest;
import com.cinema.model.request.UpdateOrderRequest;
import com.cinema.model.Item;
import com.cinema.model.ItemType;
import com.cinema.model.Order;
import com.cinema.model.OrderStatus;
import com.cinema.repository.OrderRepository;
import com.cinema.rules.OrderRules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * TODO: Implementar la lógica de negocio.
 *
 * Reglas:
 * - El "total" se calcula automáticamente (suma de precios de items)
 * - Solo se puede eliminar una orden si su status es PENDING
 * - El PATCH solo permite modificar "items" y "status"
 * - Al crear, asignar UUID, status PENDING y timestamp
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderHelper orderHelper;

    private final OrderRules orderRules;

    private final OrderRepository repository;

    public Order createOrder(String country, CreateOrderRequest request) {
        List<Item> items = orderHelper.toItems(request.items());
        orderRules.requireTicket(items);

        Order order = new Order(
                UUID.randomUUID().toString(),
                orderHelper.normalizeCountry(country),
                String.valueOf(request.cinemaId()),
                request.userId().trim(),
                items,
                orderRules.calculateTotal(items),
                OrderStatus.PENDING,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return repository.save(order);
    }

    public Order getOrder(String country, String orderId) {
        String normalizedCountry = orderHelper.normalizeCountry(country);

        // 404 if it is missing the order.
        return repository.findById(orderHelper.normalizeId(orderId))
                .filter(order -> order.getCountry().equals(normalizedCountry))
                .orElseThrow(() -> new OrderNotFoundException(orderId, normalizedCountry));
    }

    public Order updateOrder(String country, String orderId, UpdateOrderRequest request) {
        if (request.items() == null && request.status() == null) {
            throw new InvalidRequestException(
                    ExceptionMessages.AT_LEAST_ONE_ITEM_MUST_BE_PROVIDED);
        }

        Order existing = getOrder(country, orderId);

        List<Item> newItems = null;
        if (request.items() != null) {
            newItems = orderHelper.toItems(request.items());

            orderRules.requireTicket(newItems);
        }

        List<Item> items = newItems;
        return repository.update(existing.getOrderId(), current -> new Order(
                        current.getOrderId(),
                        current.getCountry(),
                        current.getCinemaId(),
                        current.getUserId(),
                        items != null ? items : current.getItems(),
                        items != null ? orderRules.calculateTotal(items) : current.getTotal(),
                        request.status() != null ? request.status() : current.getStatus(),
                        current.getCreatedAt()))
                .orElseThrow(() -> new OrderNotFoundException(orderId, existing.getCountry()));
    }

    public void deleteOrder(String country, String orderId) {
        Order existing = getOrder(country, orderId);
        boolean deleted = repository.deleteById(existing.getOrderId(), o -> o.getStatus() == OrderStatus.PENDING);

        if (!deleted) {
            throw new OrderNotDeletableException(existing.getOrderId(), existing.getStatus());
        }
    }

}
