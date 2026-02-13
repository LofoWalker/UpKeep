package com.upkeep.infrastructure.adapter.in.rest.budget;

public record UpdateBudgetResponse(
        String budgetId,
        long amountCents,
        String currency,
        boolean isLowerThanAllocations,
        long currentAllocationsCents
) {
}

