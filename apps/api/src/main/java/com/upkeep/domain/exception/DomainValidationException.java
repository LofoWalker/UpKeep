package com.upkeep.domain.exception;

import java.util.List;

/**
 * Thrown when domain validation rules are violated.
 */
public class DomainValidationException extends DomainException {

    private final List<FieldError> fieldErrors;

    public DomainValidationException(String message, List<FieldError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public DomainValidationException(String message) {
        this(message, List.of());
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
