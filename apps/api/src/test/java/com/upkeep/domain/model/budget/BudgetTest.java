package com.upkeep.domain.model.budget;

import com.upkeep.domain.model.company.CompanyId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Budget")
class BudgetTest {

    @Test
    @DisplayName("should create budget with generated ID and timestamps")
    void shouldCreateBudget() {
        CompanyId companyId = CompanyId.generate();
        Money amount = new Money(50000, Currency.EUR);

        Instant before = Instant.now();
        Budget budget = Budget.create(companyId, amount);
        Instant after = Instant.now();

        assertNotNull(budget.getId());
        assertEquals(companyId, budget.getCompanyId());
        assertEquals(amount, budget.getAmount());
        assertNotNull(budget.getEffectiveFrom());
        assertTrue(budget.getCreatedAt().compareTo(before) >= 0);
        assertTrue(budget.getCreatedAt().compareTo(after) <= 0);
        assertEquals(budget.getCreatedAt(), budget.getUpdatedAt());
    }

    @Test
    @DisplayName("should reconstitute budget from persisted data")
    void shouldReconstituteBudget() {
        BudgetId id = BudgetId.generate();
        CompanyId companyId = CompanyId.generate();
        Money amount = new Money(50000, Currency.EUR);
        Instant effectiveFrom = Instant.now().minusSeconds(86400);
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now();

        Budget budget = Budget.reconstitute(id, companyId, amount, effectiveFrom, createdAt, updatedAt);

        assertEquals(id, budget.getId());
        assertEquals(companyId, budget.getCompanyId());
        assertEquals(amount, budget.getAmount());
        assertEquals(effectiveFrom, budget.getEffectiveFrom());
        assertEquals(createdAt, budget.getCreatedAt());
        assertEquals(updatedAt, budget.getUpdatedAt());
    }

    @Test
    @DisplayName("should update budget amount and timestamp")
    void shouldUpdateAmount() {
        CompanyId companyId = CompanyId.generate();
        Money originalAmount = new Money(50000, Currency.EUR);
        Budget budget = Budget.create(companyId, originalAmount);

        Instant originalUpdatedAt = budget.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Money newAmount = new Money(100000, Currency.EUR);
        budget.updateAmount(newAmount);

        assertEquals(newAmount, budget.getAmount());
        assertTrue(budget.getUpdatedAt().isAfter(originalUpdatedAt));
    }
}
