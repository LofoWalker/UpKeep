package com.upkeep.infrastructure.adapter.in.rest.budget;
public record BudgetResponse(
        String budgetId,
        long amountCents,
        String currency
) {
}
