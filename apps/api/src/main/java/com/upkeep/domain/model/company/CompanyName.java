package com.upkeep.domain.model.company;

import com.upkeep.domain.exception.DomainValidationException;
import com.upkeep.domain.exception.FieldError;

import java.util.List;

public record CompanyName(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;

    public CompanyName {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("Company name cannot be empty", List.of(
                    new FieldError("name", "Company name cannot be empty")
            ));
        }
        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            throw new DomainValidationException(
                    "Company name must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters",
                    List.of(new FieldError("name",
                            "Company name must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters"))
            );
        }
    }

    public static CompanyName from(String value) {
        return new CompanyName(value.trim());
    }

    @Override
    public String toString() {
        return value;
    }
}
