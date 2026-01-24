package com.upkeep.domain.model.customer;

import java.util.UUID;

public record CustomerId(UUID value) {
    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }

    public static CustomerId from(String value) {
        return new CustomerId(UUID.fromString(value));
    }

    public static CustomerId from(UUID value) {
        return new CustomerId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
