package com.upkeep.domain.model.customer;

import com.upkeep.domain.exception.ValidationException;
import com.upkeep.infrastructure.adapter.in.rest.response.ApiError;

import java.util.List;
import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Email is required", List.of(
                new ApiError.FieldError("email", "Email is required")
            ));
        }

        String normalizedValue = value.toLowerCase().trim();
        if (!EMAIL_PATTERN.matcher(normalizedValue).matches()) {
            throw new ValidationException("Invalid email format", List.of(
                new ApiError.FieldError("email", "Invalid email format")
            ));
        }

        value = normalizedValue;
    }
}
