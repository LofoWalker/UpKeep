package com.upkeep.application.port.in.budget;

import com.upkeep.domain.model.budget.Currency;

public interface UpdateCompanyBudgetUseCase {

    UpdateBudgetResult execute(UpdateBudgetCommand command);

    record UpdateBudgetCommand(
            String companyId,
            String actorUserId,
            long newAmountCents,
            Currency currency
    ) {}

    record UpdateBudgetResult(
            String budgetId,
            long amountCents,
            String currency,
            boolean isLowerThanAllocations,
            long currentAllocationsCents
    ) {}
}

