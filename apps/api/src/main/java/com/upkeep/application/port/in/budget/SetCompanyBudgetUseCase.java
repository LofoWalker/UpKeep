package com.upkeep.application.port.in.budget;

import com.upkeep.domain.model.budget.Currency;

public interface SetCompanyBudgetUseCase {

    SetBudgetResult execute(SetBudgetCommand command);

    record SetBudgetCommand(
            String companyId,
            String actorUserId,
            long amountCents,
            Currency currency
    ) {}

    record SetBudgetResult(
            String budgetId,
            long amountCents,
            String currency
    ) {}
}
