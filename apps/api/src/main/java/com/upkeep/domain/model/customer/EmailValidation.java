package com.upkeep.domain.model.customer;

import java.util.regex.Pattern;

/**
 * Centralized email validation constants and utilities.
 * Single source of truth for email format validation across the application.
 */
public final class EmailValidation {

    public static final String EMAIL_REGEX = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private EmailValidation() {
        // Utility class - prevent instantiation
    }

    public static boolean isValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
