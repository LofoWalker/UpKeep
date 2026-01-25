package com.upkeep.domain.exception;

/**
 * Base exception for all domain-specific business rule violations.
 * Domain exceptions are pure business concepts without HTTP semantics.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
