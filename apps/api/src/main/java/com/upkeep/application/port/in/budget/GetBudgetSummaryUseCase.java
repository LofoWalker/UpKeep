package com.upkeep.application.port.in.budget;

public interface GetBudgetSummaryUseCase {

    BudgetSummary execute(String companyId);

    record BudgetSummary(
            String budgetId,
            long totalCents,
            long allocatedCents,
            long remainingCents,
            String currency,
            boolean exists
    ) {
        public static BudgetSummary empty() {
            return new BudgetSummary(null, 0, 0, 0, "EUR", false);
        }

        public static BudgetSummary of(String budgetId, long totalCents, String currency) {
            return new BudgetSummary(budgetId, totalCents, 0, totalCents, currency, true);
        }
    }
}
