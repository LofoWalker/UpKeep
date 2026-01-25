package com.upkeep.domain.model.customer;

import com.upkeep.domain.exception.DomainValidationException;
import com.upkeep.domain.exception.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public record Password(String value) {
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern NUMBER = Pattern.compile("[0-9]");

    public Password {
        List<FieldError> errors = new ArrayList<>();

        if (value == null || value.isBlank()) {
            errors.add(new FieldError("password", "Password is required"));
        } else {
            if (value.length() < MIN_LENGTH) {
                errors.add(new FieldError("password", "Password must be at least 8 characters"));
            }
            if (!UPPERCASE.matcher(value).find()) {
                errors.add(new FieldError("password", "Password must contain at least one uppercase letter"));
            }
            if (!NUMBER.matcher(value).find()) {
                errors.add(new FieldError("password", "Password must contain at least one number"));
            }
        }

        if (!errors.isEmpty()) {
            throw new DomainValidationException("Invalid password", errors);
        }
    }
}
