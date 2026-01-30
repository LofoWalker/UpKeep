package com.upkeep.domain.model.budget;

import com.upkeep.domain.model.company.CompanyId;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;

/**
 * Budget entity representing a company's monthly open-source budget.
 */
public class Budget {
    private final BudgetId id;
    private final CompanyId companyId;
    private Money amount;
    private final Instant effectiveFrom;
    private final Instant createdAt;
    private Instant updatedAt;

    private Budget(BudgetId id,
                   CompanyId companyId,
                   Money amount,
                   Instant effectiveFrom,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.amount = amount;
        this.effectiveFrom = effectiveFrom;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Budget create(CompanyId companyId, Money amount) {
        Instant now = Instant.now();
        Instant firstDayOfMonth = YearMonth.now()
                .atDay(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        return new Budget(
                BudgetId.generate(),
                companyId,
                amount,
                firstDayOfMonth,
                now,
                now
        );
    }

    public static Budget reconstitute(BudgetId id,
                                      CompanyId companyId,
                                      Money amount,
                                      Instant effectiveFrom,
                                      Instant createdAt,
                                      Instant updatedAt) {
        return new Budget(id, companyId, amount, effectiveFrom, createdAt, updatedAt);
    }

    public void updateAmount(Money newAmount) {
        this.amount = newAmount;
        this.updatedAt = Instant.now();
    }

    public BudgetId getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public Money getAmount() {
        return amount;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
