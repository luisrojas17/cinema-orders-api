package com.cinema.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Combo discount.
 *
 * Given the products a customer bought, the combo rules and the discount of each combo, computes
 * the maximum total discount where every combo is applied at most once and every purchased unit
 * belongs to at most one combo.
 *
 * Approach: with at most 10 combos, backtracking over "apply / skip" for each combo explores at most
 * 2^10 = 1024 branches. Stock is tracked as a count per product, so repeated products
 * (e.g. two sodas) and combos that need the same product more than once are handled correctly.
 *
 * Example: purchased = [popcorn, soda, nachos, popcorn, soda],
 *          combos = [[popcorn, soda], [nachos, soda]], discounts = [20, 15] -> 35
 */
public class DiscountCalculator {

    /**
     * @param purchased products bought by the customer
     * @param combos    each combo is an array of required products
     * @param discounts discount of each combo (same length as combos)
     * @return maximum total applicable discount
     * @throws IllegalArgumentException if an argument is null or sizes do not match
     */
    public int maxDiscount(String[] purchased, String[][] combos, int[] discounts) {
        if (purchased == null || combos == null || discounts == null) {
            throw new IllegalArgumentException("purchased, combos and discounts must not be null");
        }
        if (combos.length != discounts.length) {
            throw new IllegalArgumentException("combos and discounts must have the same length");
        }

        Map<String, Integer> stock = count(purchased);
        List<Map<String, Integer>> required = new ArrayList<>(combos.length);
        for (String[] combo : combos) {
            if (combo == null) {
                throw new IllegalArgumentException("a combo must not be null");
            }
            required.add(count(combo));
        }
        return search(0, stock, required, discounts);
    }

    private int search(int index, Map<String, Integer> stock, List<Map<String, Integer>> required, int[] discounts) {
        if (index == required.size()) {
            return 0;
        }
        int best = search(index + 1, stock, required, discounts); // skip this combo

        Map<String, Integer> need = required.get(index);
        if (isAvailable(stock, need)) {                            // apply this combo
            adjust(stock, need, -1);
            best = Math.max(best, discounts[index] + search(index + 1, stock, required, discounts));
            adjust(stock, need, +1);
        }
        return best;
    }

    private boolean isAvailable(Map<String, Integer> stock, Map<String, Integer> need) {
        return need.entrySet().stream()
                .allMatch(e -> stock.getOrDefault(e.getKey(), 0) >= e.getValue());
    }

    private void adjust(Map<String, Integer> stock, Map<String, Integer> need, int sign) {
        need.forEach((product, qty) -> stock.merge(product, sign * qty, Integer::sum));
    }

    private Map<String, Integer> count(String[] products) {
        Map<String, Integer> counts = new HashMap<>();
        for (String product : products) {
            counts.merge(product, 1, Integer::sum);
        }
        return counts;
    }
}
