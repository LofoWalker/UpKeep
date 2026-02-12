package com.upkeep.application.usecase;

import com.upkeep.application.port.in.budget.GetBudgetSummaryUseCase;
import com.upkeep.application.port.out.budget.BudgetRepository;
import com.upkeep.domain.model.company.CompanyId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetBudgetSummaryUseCaseImpl implements GetBudgetSummaryUseCase {

    private final BudgetRepository budgetRepository;

    @Inject
    public GetBudgetSummaryUseCaseImpl(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public BudgetSummary execute(String companyId) {
        CompanyId id = CompanyId.from(companyId);

        return budgetRepository.findByCompanyId(id)
                .map(budget -> BudgetSummary.of(
                        budget.getId().toString(),
                        budget.getAmount().amountCents(),
                        budget.getAmount().currency().name()
                ))
                .orElse(BudgetSummary.empty());
    }
}
