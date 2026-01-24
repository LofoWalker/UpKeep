package com.upkeep.domain.exception;

import java.util.List;

public class ValidationException extends ApiException {
    private final List<FieldError> fieldErrors;

    public ValidationException(String message, List<FieldError> fieldErrors) {
        super("VALIDATION_ERROR", message, 400);
        this.fieldErrors = fieldErrors;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
