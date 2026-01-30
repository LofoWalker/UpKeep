package com.upkeep.application.usecase;

import com.upkeep.application.port.in.budget.GetBudgetSummaryUseCase;
import com.upkeep.application.port.in.budget.GetBudgetSummaryUseCase.BudgetSummary;
import com.upkeep.application.port.out.budget.BudgetRepository;
import com.upkeep.domain.model.budget.Budget;
import com.upkeep.domain.model.budget.Currency;
import com.upkeep.domain.model.budget.Money;
import com.upkeep.domain.model.company.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GetBudgetSummaryUseCase")
class GetBudgetSummaryUseCaseImplTest {

    private BudgetRepository budgetRepository;
    private GetBudgetSummaryUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        budgetRepository = mock(BudgetRepository.class);
        useCase = new GetBudgetSummaryUseCaseImpl(budgetRepository);
    }

    @Test
    @DisplayName("should return budget summary when budget exists")
    void shouldReturnBudgetSummary() {
        String companyId = UUID.randomUUID().toString();
        CompanyId id = CompanyId.from(companyId);

        Budget budget = Budget.create(id, new Money(50000L, Currency.EUR));
        when(budgetRepository.findByCompanyId(any(CompanyId.class)))
            .thenReturn(Optional.of(budget));

        BudgetSummary summary = useCase.execute(companyId);

        assertNotNull(summary);
        assertTrue(summary.exists());
        assertEquals(budget.getId().toString(), summary.budgetId());
        assertEquals(50000L, summary.totalCents());
        assertEquals(0L, summary.allocatedCents());
        assertEquals(50000L, summary.remainingCents());
        assertEquals("EUR", summary.currency());
    }

    @Test
    @DisplayName("should return empty summary when budget does not exist")
    void shouldReturnEmptySummary() {
        String companyId = UUID.randomUUID().toString();

        when(budgetRepository.findByCompanyId(any(CompanyId.class)))
            .thenReturn(Optional.empty());

        BudgetSummary summary = useCase.execute(companyId);

        assertNotNull(summary);
        assertFalse(summary.exists());
        assertNull(summary.budgetId());
        assertEquals(0L, summary.totalCents());
        assertEquals(0L, summary.allocatedCents());
        assertEquals(0L, summary.remainingCents());
        assertEquals("EUR", summary.currency());
    }

    @Test
    @DisplayName("should return summary with different currencies")
    void shouldReturnSummaryWithDifferentCurrencies() {
        String companyId = UUID.randomUUID().toString();
        CompanyId id = CompanyId.from(companyId);

        Budget budget = Budget.create(id, new Money(100000L, Currency.USD));
        when(budgetRepository.findByCompanyId(any(CompanyId.class)))
            .thenReturn(Optional.of(budget));

        BudgetSummary summary = useCase.execute(companyId);

        assertEquals("USD", summary.currency());
        assertEquals(100000L, summary.totalCents());
    }
}
