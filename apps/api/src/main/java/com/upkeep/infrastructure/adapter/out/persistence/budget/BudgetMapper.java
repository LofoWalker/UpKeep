package com.upkeep.infrastructure.adapter.out.persistence.budget;

import com.upkeep.domain.model.budget.Budget;
import com.upkeep.domain.model.budget.BudgetId;
import com.upkeep.domain.model.budget.Currency;
import com.upkeep.domain.model.budget.Money;
import com.upkeep.domain.model.company.CompanyId;

public final class BudgetMapper {

    private BudgetMapper() {
    }

    public static BudgetEntity toEntity(Budget budget) {
        BudgetEntity entity = new BudgetEntity();
        entity.id = budget.getId().value();
        entity.companyId = budget.getCompanyId().value();
        entity.amountCents = budget.getAmount().amountCents();
        entity.currency = budget.getAmount().currency().name();
        entity.effectiveFrom = budget.getEffectiveFrom();
        entity.createdAt = budget.getCreatedAt();
        entity.updatedAt = budget.getUpdatedAt();
        return entity;
    }

    public static Budget toDomain(BudgetEntity entity) {
        return Budget.reconstitute(
                BudgetId.from(entity.id),
                CompanyId.from(entity.companyId),
                new Money(entity.amountCents, Currency.valueOf(entity.currency)),
                entity.effectiveFrom,
                entity.createdAt,
                entity.updatedAt
        );
    }
}
