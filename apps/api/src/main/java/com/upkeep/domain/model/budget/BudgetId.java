package com.upkeep.domain.model.budget;

import java.util.UUID;

public record BudgetId(UUID value) {
    public static BudgetId generate() {
        return new BudgetId(UUID.randomUUID());
    }

    public static BudgetId from(String value) {
        return new BudgetId(UUID.fromString(value));
    }

    public static BudgetId from(UUID value) {
        return new BudgetId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
