package com.upkeep.infrastructure.adapter.in.rest.budget;
public record BudgetSummaryResponse(
        String budgetId,
        long totalCents,
        long allocatedCents,
        long remainingCents,
        String currency,
        boolean exists
) {
}
