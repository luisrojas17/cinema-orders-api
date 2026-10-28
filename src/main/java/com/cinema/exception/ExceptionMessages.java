package com.cinema.exception;

public interface ExceptionMessages {

    String ORDER_NOT_FOUND = "Order '%s' not found for country '%s'";
    String AT_LEAST_ONE_ITEM_MUST_BE_PROVIDED = "At least one of 'items' or 'status' must be provided";
    String A_TICKET_IS_REQUIRED = "An order must contain at least one item of type TICKET";

}
