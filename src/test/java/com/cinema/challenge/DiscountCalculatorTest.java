package com.cinema.challenge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscountCalculatorTest {

    private DiscountCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DiscountCalculator();
    }

    @Test
    @DisplayName("Aplica ambos combos cuando hay suficientes productos")
    void shouldApplyMultipleCombos() {
        String[] purchased = {"popcorn", "soda", "nachos", "popcorn", "soda"};
        String[][] combos = {{"popcorn", "soda"}, {"nachos", "soda"}};
        int[] discounts = {20, 15};

        assertEquals(35, calculator.maxDiscount(purchased, combos, discounts));
    }

    @Test
    @DisplayName("No aplica combo si faltan productos")
    void shouldNotApplyComboWhenMissingProducts() {
        String[] purchased = {"soda"};
        String[][] combos = {{"popcorn", "soda"}};
        int[] discounts = {20};

        assertEquals(0, calculator.maxDiscount(purchased, combos, discounts));
    }

    @Test
    @DisplayName("Prioriza el combo con mayor descuento")
    void shouldPrioritizeHigherDiscount() {
        String[] purchased = {"popcorn", "soda"};
        String[][] combos = {{"popcorn", "soda"}, {"popcorn", "soda"}};
        int[] discounts = {10, 25};

        assertEquals(25, calculator.maxDiscount(purchased, combos, discounts));
    }

    @Test
    @DisplayName("No reutiliza productos entre combos")
    void shouldNotReuseProducts() {
        String[] purchased = {"popcorn", "soda"};
        String[][] combos = {{"popcorn", "soda"}, {"popcorn", "soda"}};
        int[] discounts = {20, 15};

        assertEquals(20, calculator.maxDiscount(purchased, combos, discounts));
    }

    @Test
    @DisplayName("Combo con productos duplicados requiere cantidad suficiente")
    void shouldHandleDuplicateProductsInCombo() {
        String[] purchased = {"soda", "soda", "soda"};
        String[][] combos = {{"soda", "soda"}};
        int[] discounts = {10};

        assertEquals(10, calculator.maxDiscount(purchased, combos, discounts));
    }

    @Test
    @DisplayName("Sin productos comprados, descuento es 0")
    void shouldReturnZeroForEmptyPurchase() {
        String[] purchased = {};
        String[][] combos = {{"popcorn", "soda"}};
        int[] discounts = {20};

        assertEquals(0, calculator.maxDiscount(purchased, combos, discounts));
    }
}
