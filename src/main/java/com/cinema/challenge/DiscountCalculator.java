package com.cinema.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ejercicio: Descuento por Combo
 *
 * Una cadena de cines tiene una regla de combos: si un cliente compra ciertos
 * productos juntos, recibe un descuento. Dado un listado de productos comprados
 * y las reglas de combo, calcula el descuento total máximo que aplica.
 *
 * Reglas:
 * - Cada combo puede aplicarse UNA sola vez
 * - Un producto solo puede pertenecer a un combo (no se reutiliza)
 * - Se busca el descuento MÁXIMO posible
 *
 * Ejemplo 1:
 *   purchased = ["popcorn", "soda", "nachos", "popcorn", "soda"]
 *   combos = [["popcorn", "soda"], ["nachos", "soda"]]
 *   discounts = [20, 15]
 *   Output: 35
 *
 * Ejemplo 2:
 *   purchased = ["soda"]
 *   combos = [["popcorn", "soda"]]
 *   discounts = [20]
 *   Output: 0 (no tiene popcorn, no aplica el combo)
 *
 * Constraints:
 *   1 <= purchased.length <= 50
 *   1 <= combos.length <= 10
 *   1 <= combos[i].length <= 5
 *   discounts.length == combos.length
 */
public class DiscountCalculator {

    private static final int MAX_PURCHASED = 50;
    private static final int MAX_COMBOS = 10;
    private static final int MAX_COMBO_SIZE = 5;

    /**
     * To get the max of discount according to the products and combos bought.
     *
     * @param purchased products bought by the customer
     * @param combos    each combo is an array of required products
     * @param discounts discount of each combo (same length as combos)
     * @return maximum total applicable discount
     *
     * @throws IllegalArgumentException if an argument is null or sizes do not match
     */
    public int maxDiscount(String[] purchased, String[][] combos, int[] discounts) {

        // To apply validations
        constraints(purchased, combos, discounts);

        // Products
        Map<String, Integer> stock = count(purchased);

        // Combos with its quantity required
        List<Map<String, Integer>> required = new ArrayList<>(combos.length);

        for (String[] combo : combos) {
            required.add(count(combo));
        }

        return search(0, stock, required, discounts);
    }

    /**
     * To apply constraints given.
     *
     * @param purchased a string array with products bought.
     * @param combos a string matrix with combos.
     * @param discounts an array with discounts by combo to apply.
     */
    private void constraints(String[] purchased, String[][] combos, int[] discounts) {
        if (purchased == null || combos == null || discounts == null) {
            throw new IllegalArgumentException("purchased, combos and discounts must not be null");
        }

        if (purchased.length > MAX_PURCHASED) {
            throw new IllegalArgumentException("purchased must have at most " + MAX_PURCHASED + " products");
        }

        for (String product : purchased) {
            if (product == null) {
                throw new IllegalArgumentException("purchased must not contain null products");
            }
        }

        if (combos.length < 1 || combos.length > MAX_COMBOS) {
            throw new IllegalArgumentException("combos must have between 1 and " + MAX_COMBOS + " entries");
        }

        if (discounts.length != combos.length) {
            throw new IllegalArgumentException("discounts must have the same length as combos");
        }

        for (String[] combo : combos) {
            if (combo == null || combo.length < 1 || combo.length > MAX_COMBO_SIZE) {
                throw new IllegalArgumentException(
                        "each combo must have between 1 and " + MAX_COMBO_SIZE + " products");
            }

            for (String product : combo) {
                if (product == null) {
                    throw new IllegalArgumentException("a combo must not contain null products");
                }
            }
        }
    }

    /**
     * To get the number of products per category. For example:
     * <br/>
     * Map<product, quantity> -> {soda=2, popcorn=1}
     *
     * @param products an array of products.
     *
     * @return a map with the product and its quantity per category.
     */
    private Map<String, Integer> count(String[] products) {
        Map<String, Integer> counts = new HashMap<>();
        for (String product : products) {
            counts.merge(product, 1, Integer::sum);
        }
        return counts;
    }

    /**
     * Returns the maximum discount obtainable from the combos at positions {@code index} onwards,
     * given the units still available in {@code stock}.
     *
     * <p>For each combo there are two options, and the best of both is kept:
     * <ol>
     *   <li>skip the combo and move on to the next one;</li>
     *   <li>apply the combo, if the stock covers it: consume its units, add its discount to what the
     *       remaining combos can still obtain, and restore the stock afterwards (backtracking).</li>
     * </ol>
     * Exploring both options, instead of always taking the highest discount first, is what avoids
     * greedy mistakes: a large combo can consume units that two smaller combos would have used
     * to give a larger total.
     *
     * @param index     position of the combo being decided; {@code required.size()} is the base case
     * @param stock     units still available per product; modified during the search but always
     *                  restored before returning
     * @param required  units required by each combo, per product
     * @param discounts discount of each combo
     *
     * @return best total discount from combos {@code index} to the last one
     */
    private int search(int index, Map<String, Integer> stock,
                       List<Map<String, Integer>> required, int[] discounts) {

        if (index == required.size()) {
            return 0;
        }

        int best = search(index + 1, stock, required, discounts); // skip this combo

        Map<String, Integer> need = required.get(index);
        if (isAvailable(stock, need)) {                                 // the inventory has enough units to consume?
            adjust(stock, need, -1);                               // consume units

            // To appy the discount sum of the combo according to best possible result
            best = Math.max(best, discounts[index] + search(index + 1, stock, required, discounts));

            // To restore the stock in order to the recursivity comes back.
            adjust(stock, need, +1);
        }

        return best;
    }

    /**
     * Checks whether the stock has enough units of every product the combo requires.
     *
     * @param stock units available per product
     * @param need  units required per product by the combo
     * @return true if every required product is available in sufficient quantity
     */
    private boolean isAvailable(Map<String, Integer> stock, Map<String, Integer> need) {
        return need.entrySet().stream()
                .allMatch(e -> stock.getOrDefault(e.getKey(), 0) >= e.getValue());
    }

    /**
     * Consumes or returns the units a combo requires.
     *
     * @param stock units available per product; modified in place
     * @param need  units required per product by the combo
     * @param sign  -1 to consume the units (apply the combo), +1 to give them back (backtrack)
     */
    private void adjust(Map<String, Integer> stock, Map<String, Integer> need, int sign) {
        need.forEach((product, qty) -> stock.merge(product, sign * qty, Integer::sum));
    }

}
