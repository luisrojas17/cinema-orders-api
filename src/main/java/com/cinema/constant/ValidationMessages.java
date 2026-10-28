package com.cinema.constant;

public interface ValidationMessages {
    String COUNTRY_MESSAGE = "Must be a 2-letter country code (e.g. MX)";
    String UUID_MESSAGE = "Must be a valid UUID";
    String CINEMA_ID_MESSAGE = "Invalid cinema_id. The value has to be grater than 0.";
    String ITEMS_MESSAGE = "Must contain at least one item when provided";
}
