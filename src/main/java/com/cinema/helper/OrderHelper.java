package com.cinema.helper;

import com.cinema.constant.CommonConstant;
import com.cinema.model.Item;
import com.cinema.model.request.ItemRequest;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * This is a helper class where is implemented logic to convert between data types or mapping data.
 */
@Component
public class OrderHelper {

    public List<Item> toItems(List<ItemRequest> requests) {
        return requests.stream()
                .map(r ->
                        new Item(r.type(), r.name().trim(), r.price().setScale(
                                CommonConstant.MONEY_SCALE, RoundingMode.UNNECESSARY)))
                .toList();
    }

    public String normalizeCountry(String country) {
        return country.trim().toUpperCase(Locale.ROOT);
    }

    public String normalizeId(String orderId) {
        return orderId.trim().toLowerCase(Locale.ROOT);
    }

}
