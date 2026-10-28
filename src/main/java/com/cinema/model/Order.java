package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String country;
    private String cinemaId;
    private String userId;
    private List<Item> items;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
