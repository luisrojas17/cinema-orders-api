package com.cinema.constant;

/**
 * This is an interface with validation messages constants used by validation layer
 * the in controller.
 */
public interface ValidationMessages {
    String COUNTRY_MESSAGE = "Must be a 2-letter country code (e.g. MX)";
    String UUID_MESSAGE = "Must be a valid UUID";
    String CINEMA_ID_MESSAGE = "Invalid cinema_id. The value has to be grater than 0.";
    String ITEMS_MESSAGE = "Must contain at least one item when provided";
}
