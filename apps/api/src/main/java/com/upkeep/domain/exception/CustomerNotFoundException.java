package com.upkeep.domain.exception;

/**
 * Thrown when a customer cannot be found by the given identifier.
 */
public class CustomerNotFoundException extends DomainException {

    public CustomerNotFoundException(String identifier) {
        super("Customer not found: " + identifier);
    }
}
