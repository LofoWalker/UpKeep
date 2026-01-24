package com.upkeep.domain.exception;

/**
 * Thrown when attempting to register a customer with an email that already exists.
 */
public class CustomerAlreadyExistsException extends DomainException {

    private final String email;

    public CustomerAlreadyExistsException(String email) {
        super("An account with this email already exists");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
