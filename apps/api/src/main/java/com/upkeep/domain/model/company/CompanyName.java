package com.upkeep.domain.model.company;

public record CompanyName(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;

    public CompanyName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Company name must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters");
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
