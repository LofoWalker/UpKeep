package com.upkeep.application.port.out.budget;

import com.upkeep.domain.model.budget.Budget;
import com.upkeep.domain.model.budget.BudgetId;
import com.upkeep.domain.model.company.CompanyId;

import java.time.Instant;
import java.util.Optional;

public interface BudgetRepository {
    void save(Budget budget);

    Optional<Budget> findById(BudgetId id);

    Optional<Budget> findByCompanyId(CompanyId companyId);

    Optional<Budget> findByCompanyIdAndEffectiveFrom(CompanyId companyId, Instant effectiveFrom);

    boolean existsByCompanyId(CompanyId companyId);
}
