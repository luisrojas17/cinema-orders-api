package com.cinema.rules;

import com.cinema.constant.CommonConstant;
import com.cinema.exception.InvalidOrderException;
import com.cinema.exception.constant.ExceptionMessages;
import com.cinema.model.Item;
import com.cinema.model.ItemType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * This is a business rules class were are implemented business rules logic
 * regarding order management.
 */
@Component
public class OrderRules {

    public void requireTicket(List<Item> items) {
        if (items.stream().noneMatch(item -> item.getType() == ItemType.TICKET)) {
            throw new InvalidOrderException(ExceptionMessages.A_TICKET_IS_REQUIRED);
        }
    }

    public BigDecimal calculateTotal(List<Item> items) {
        return items.stream()
                .map(Item::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(CommonConstant.MONEY_SCALE, RoundingMode.HALF_UP);
    }

}
