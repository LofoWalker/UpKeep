package com.upkeep.domain.model.customer;

import com.upkeep.domain.exception.DomainValidationException;
import com.upkeep.domain.exception.FieldError;

import java.util.List;

public record Email(String value) {

    public Email {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("Email is required", List.of(
                    new FieldError("email", "Email is required")
            ));
        }

        String normalizedValue = value.toLowerCase().trim();
        if (!EmailValidation.isValid(normalizedValue)) {
            throw new DomainValidationException("Invalid email format", List.of(
                    new FieldError("email", "Invalid email format")
            ));
        }

        value = normalizedValue;
    }
}
