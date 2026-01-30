package com.upkeep.infrastructure.adapter.out.persistence.budget;

import com.upkeep.application.port.out.budget.BudgetRepository;
import com.upkeep.domain.model.budget.Budget;
import com.upkeep.domain.model.budget.BudgetId;
import com.upkeep.domain.model.company.CompanyId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BudgetJpaRepository implements BudgetRepository, PanacheRepositoryBase<BudgetEntity, UUID> {

    @Override
    public void save(Budget budget) {
        BudgetEntity entity = BudgetMapper.toEntity(budget);
        persist(entity);
    }

    @Override
    public Optional<Budget> findById(BudgetId id) {
        return find("id", id.value())
                .firstResultOptional()
                .map(BudgetMapper::toDomain);
    }

    @Override
    public Optional<Budget> findByCompanyId(CompanyId companyId) {
        return find("companyId", companyId.value())
                .firstResultOptional()
                .map(BudgetMapper::toDomain);
    }

    @Override
    public Optional<Budget> findByCompanyIdAndEffectiveFrom(CompanyId companyId, Instant effectiveFrom) {
        return find("companyId = ?1 AND effectiveFrom = ?2", companyId.value(), effectiveFrom)
                .firstResultOptional()
                .map(BudgetMapper::toDomain);
    }

    @Override
    public boolean existsByCompanyId(CompanyId companyId) {
        return count("companyId", companyId.value()) > 0;
    }
}
