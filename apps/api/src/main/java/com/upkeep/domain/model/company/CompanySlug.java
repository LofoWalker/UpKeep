package com.upkeep.domain.model.company;

import java.util.regex.Pattern;

public record CompanySlug(String value) {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 50;

    public CompanySlug {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Company slug cannot be empty");
        }
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Company slug must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters");
        }
        if (!SLUG_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Company slug must contain only lowercase letters, numbers, and hyphens");
        }
    }

    public static CompanySlug from(String value) {
        return new CompanySlug(value.toLowerCase().trim());
    }

    public static CompanySlug fromName(String companyName) {
        String slug = companyName.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (slug.length() < MIN_LENGTH) {
            slug = slug + "-co";
        }
        if (slug.length() > MAX_LENGTH) {
            slug = slug.substring(0, MAX_LENGTH).replaceAll("-$", "");
        }

        return new CompanySlug(slug);
    }

    @Override
    public String toString() {
        return value;
    }
}
