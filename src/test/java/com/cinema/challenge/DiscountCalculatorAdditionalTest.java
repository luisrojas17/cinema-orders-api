package com.cinema.challenge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscountCalculatorAdditionalTest {

    private final DiscountCalculator calculator = new DiscountCalculator();

    @Test
    void example1_bothCombosApplyUsingDifferentUnitsOfSoda() {
        String[] purchased = {"popcorn", "soda", "nachos", "popcorn", "soda"};
        String[][] combos = {{"popcorn", "soda"}, {"nachos", "soda"}};
        assertEquals(35, calculator.maxDiscount(purchased, combos, new int[]{20, 15}));
    }

    @Test
    void example2_missingProductMeansNoDiscount() {
        assertEquals(0, calculator.maxDiscount(new String[]{"soda"}, new String[][]{{"popcorn", "soda"}}, new int[]{20}));
    }

    @Test
    void sharedProductOnlyBenefitsTheBestCombo() {
        String[] purchased = {"popcorn", "soda"};
        String[][] combos = {{"popcorn", "soda"}, {"soda"}};
        assertEquals(20, calculator.maxDiscount(purchased, combos, new int[]{20, 15}));
    }

    @Test
    void greedyByDiscountIsNotAlwaysOptimal() {
        String[] purchased = {"a", "b", "c", "d"};
        String[][] combos = {{"a", "b", "c"}, {"a", "b"}, {"c", "d"}};
        assertEquals(30, calculator.maxDiscount(purchased, combos, new int[]{25, 15, 15}));
    }

    @Test
    void comboRequiringTheSameProductTwiceNeedsTwoUnits() {
        String[][] combos = {{"soda", "soda"}};
        assertEquals(0, calculator.maxDiscount(new String[]{"soda"}, combos, new int[]{10}));
        assertEquals(10, calculator.maxDiscount(new String[]{"soda", "soda"}, combos, new int[]{10}));
    }

    @Test
    void eachComboAppliesOnlyOnceEvenWithPlentyOfStock() {
        String[] purchased = {"soda", "soda", "soda"};
        assertEquals(5, calculator.maxDiscount(purchased, new String[][]{{"soda"}}, new int[]{5}));
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.maxDiscount(new String[]{"a"}, new String[][]{{"a"}}, new int[]{1, 2}));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.maxDiscount(null, new String[][]{{"a"}}, new int[]{1}));
    }
}
