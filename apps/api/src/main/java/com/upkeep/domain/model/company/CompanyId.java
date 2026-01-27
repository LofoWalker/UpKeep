package com.upkeep.domain.model.company;

import java.util.UUID;

public record CompanyId(UUID value) {
    public static CompanyId generate() {
        return new CompanyId(UUID.randomUUID());
    }

    public static CompanyId from(String value) {
        return new CompanyId(UUID.fromString(value));
    }

    public static CompanyId from(UUID value) {
        return new CompanyId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
