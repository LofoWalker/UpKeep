package com.upkeep.domain.model.pkg;

import java.util.UUID;

public record PackageId(UUID value) {
    public static PackageId generate() {
        return new PackageId(UUID.randomUUID());
    }

    public static PackageId from(String value) {
        return new PackageId(UUID.fromString(value));
    }

    public static PackageId from(UUID value) {
        return new PackageId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

