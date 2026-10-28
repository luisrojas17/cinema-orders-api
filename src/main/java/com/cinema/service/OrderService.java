package com.cinema.service;

import com.cinema.exception.*;
import com.cinema.model.request.CreateOrderRequest;
import com.cinema.model.request.ItemRequest;
import com.cinema.model.request.UpdateOrderRequest;
import com.cinema.model.Item;
import com.cinema.model.ItemType;
import com.cinema.model.Order;
import com.cinema.model.OrderStatus;
import com.cinema.repository.OrderRepository;
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

    private static final int MONEY_SCALE = 2;

    private final OrderRepository repository;

    public Order createOrder(String country, CreateOrderRequest request) {
        List<Item> items = toItems(request.items());
        requireTicket(items);

        Order order = new Order(
                UUID.randomUUID().toString(),
                normalizeCountry(country),
                String.valueOf(request.cinemaId()),
                request.userId().trim(),
                items,
                calculateTotal(items),
                OrderStatus.PENDING,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return repository.save(order);
    }

    /**
     * Gets an order if this is found it. If the order is not found will be 404 if missing or country mismatch
     *
     * @param country
     * @param orderId
     *
     * @return an instance of Order or
     */
    public Order getOrder(String country, String orderId) {
        String normalizedCountry = normalizeCountry(country);
        return repository.findById(normalizeId(orderId))
                .filter(order -> order.getCountry().equals(normalizedCountry))
                .orElseThrow(() -> new OrderNotFoundException(orderId, normalizedCountry));
    }

    public Order updateOrder(String country, String orderId, UpdateOrderRequest request) {
        if (request.items() == null && request.status() == null) {
            throw new InvalidRequestException(
                    ExceptionMessages.AT_LEAST_ONE_ITEM_MUST_BE_PROVIDED);
        }

        // 404 if missing or country mismatch
        Order existing = getOrder(country, orderId);

        List<Item> newItems = null;
        if (request.items() != null) {
            newItems = toItems(request.items());
            requireTicket(newItems);
        }

        List<Item> items = newItems;
        return repository.update(existing.getOrderId(), current -> new Order(
                        current.getOrderId(),
                        current.getCountry(),
                        current.getCinemaId(),
                        current.getUserId(),
                        items != null ? items : current.getItems(),
                        items != null ? calculateTotal(items) : current.getTotal(),
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

    private List<Item> toItems(List<ItemRequest> requests) {
        return requests.stream()
                .map(r -> new Item(r.type(), r.name().trim(), r.price().setScale(MONEY_SCALE, RoundingMode.UNNECESSARY)))
                .toList();
    }

    private void requireTicket(List<Item> items) {
        if (items.stream().noneMatch(item -> item.getType() == ItemType.TICKET)) {
            throw new InvalidOrderException(ExceptionMessages.A_TICKET_IS_REQUIRED);
        }
    }

    private BigDecimal calculateTotal(List<Item> items) {
        return items.stream()
                .map(Item::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public String normalizeCountry(String country) {
        return country.trim().toUpperCase(Locale.ROOT);
    }

    public String normalizeId(String orderId) {
        return orderId.trim().toLowerCase(Locale.ROOT);
    }
}
