package com.cinema.repository;

import com.cinema.model.Order;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * TODO: Implementar repositorio in-memory usando ConcurrentHashMap.
 *
 * Métodos sugeridos:
 * - save(Order order): Order
 * - findById(String orderId): Optional<Order>
 * - deleteById(String orderId): void
 */
@Repository
public class OrderRepository {

    private final ConcurrentHashMap<String, Order> store = new ConcurrentHashMap<>();

    /**
     * Save an order.
     *
     * @param order the order with all values required to be saved.
     *
     * @return the order saved.
     */
    public Order save(Order order) {
        store.put(order.getOrderId(), order);
        return order;
    }

    /**
     * Find the Order.
     *
     * @param orderId
     *
     * @return the order found it.
     */
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    /**
     * Atomically replaces an existing order with the result of {@code updater}.
     *
     * @param orderId
     * @param updater
     *
     * @return true if the order was deleted, false in ny other case.
     */
    public Optional<Order> update(String orderId, UnaryOperator<Order> updater) {
        return Optional.ofNullable(store.computeIfPresent(orderId, (id, current) -> updater.apply(current)));
    }

    /**
     * Atomically removes the order only if it satisfies {@code condition}.
     *
     * @return true if the order was removed
     */
    public boolean deleteById(String orderId, Predicate<Order> condition) {
        AtomicBoolean removed = new AtomicBoolean(false);
        store.computeIfPresent(orderId, (id, current) -> {
            if (condition.test(current)) {
                removed.set(true);
                return null;
            }
            return current;
        });
        return removed.get();
    }
}
