package com.upkeep.domain.exception;

/**
 * Thrown when authentication credentials are invalid (wrong email or password).
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
