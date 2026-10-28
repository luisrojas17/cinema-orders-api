package com.cinema.challenge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscountCalculatorAdditionalTest {

    private DiscountCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DiscountCalculator();
    }

    /**
     * Taking the highest discount first is not always optimal.
     *
     * <p>Why it is needed: {@code {a,b,c}=25} consumes units that {@code {a,b}=15} and
     * {@code {c,d}=15} would use together for 30. A greedy implementation (sort by discount and
     * take in that order) passes every challenge test but returns 25 here, so this is the only test
     * that tells a correct algorithm from a greedy one. It protects the rule "the maximum possible
     * discount is sought".
     */
    @Test
    void getTheMostOptimalDiscount() {
        String[] purchased = {"a", "b", "c", "d"};
        String[][] combos = {{"a", "b", "c"}, {"a", "b"}, {"c", "d"}};
        assertEquals(30, calculator.maxDiscount(purchased, combos, new int[]{25, 15, 15}));
    }

    /**
     * A combo that requires the same product twice is not formed with a single unit.
     *
     * <p>Why it is needed: the challenge tests only cover the case where the quantity is enough.
     * This one catches the typical mistake of checking that a product is present instead of
     * checking that there are enough units of it.
     */
    @Test
    void comboRequiringTheSameProductTwiceDoesNotApplyWithASingleUnit() {
        String[][] combos = {{"soda", "soda"}};
        assertEquals(0, calculator.maxDiscount(new String[]{"soda"}, combos, new int[]{10}));
    }

    /**
     * Arguments that violate the challenge constraints are rejected with
     * {@link IllegalArgumentException}.
     *
     * <p>Why it is needed: the challenge suite does not cover input validation, so without this
     * test the checks on nulls, sizes and lengths could be removed or broken unnoticed. Covered:
     * null arguments, {@code purchased.length <= 50}, {@code 1 <= combos.length <= 10},
     * {@code 1 <= combos[i].length <= 5} and {@code discounts.length == combos.length}.
     */
    @Test
    void rejectsConstraintViolations() {
        String[] oneProduct = {"soda"};
        String[][] oneCombo = {{"soda"}};
        int[] oneDiscount = {1};

        // null arguments
        assertThrows(IllegalArgumentException.class, () ->
                calculator.maxDiscount(null, oneCombo, oneDiscount));
        assertThrows(IllegalArgumentException.class, () ->
                calculator.maxDiscount(new String[]{null}, oneCombo, oneDiscount));
        // purchased.length <= 50
        assertThrows(IllegalArgumentException.class, () ->
                calculator.maxDiscount(new String[51], oneCombo, oneDiscount));
        // 1 <= combos.length <= 10
        assertThrows(IllegalArgumentException.class, () ->
                calculator.maxDiscount(oneProduct, new String[0][], new int[0]));
        assertThrows(IllegalArgumentException.class, () ->
                calculator.maxDiscount(oneProduct, new String[11][], new int[11]));
        // 1 <= combos[i].length <= 5
        assertThrows(IllegalArgumentException.class, () ->
                calculator.maxDiscount(oneProduct, new String[][]{{}}, oneDiscount));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.maxDiscount(
                        oneProduct,
                        new String[][]{{"soda", "soda", "soda", "soda", "soda", "soda"}}, oneDiscount));
        // discounts.length == combos.length
        assertThrows(IllegalArgumentException.class, () ->
                calculator.maxDiscount(oneProduct, oneCombo, new int[]{1, 2}));
    }
}
