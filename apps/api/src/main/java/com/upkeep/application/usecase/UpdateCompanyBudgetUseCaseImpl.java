package com.upkeep.application.usecase;

import com.upkeep.application.port.in.budget.UpdateCompanyBudgetUseCase;
import com.upkeep.application.port.out.audit.AuditEventRepository;
import com.upkeep.application.port.out.budget.BudgetRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.BudgetNotFoundException;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.exception.UnauthorizedOperationException;
import com.upkeep.domain.model.audit.AuditEvent;
import com.upkeep.domain.model.budget.Budget;
import com.upkeep.domain.model.budget.Money;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UpdateCompanyBudgetUseCaseImpl implements UpdateCompanyBudgetUseCase {

    private final BudgetRepository budgetRepository;
    private final AuditEventRepository auditEventRepository;
    private final MembershipRepository membershipRepository;

    @Inject
    public UpdateCompanyBudgetUseCaseImpl(BudgetRepository budgetRepository,
                                          AuditEventRepository auditEventRepository,
                                          MembershipRepository membershipRepository) {
        this.budgetRepository = budgetRepository;
        this.auditEventRepository = auditEventRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    @Transactional
    public UpdateBudgetResult execute(UpdateBudgetCommand command) {
        CompanyId companyId = CompanyId.from(command.companyId());
        CustomerId actorId = CustomerId.from(command.actorUserId());

        Membership membership = membershipRepository.findByCustomerIdAndCompanyId(actorId, companyId)
                .orElseThrow(() -> new MembershipNotFoundException(command.actorUserId(), command.companyId()));

        if (!membership.isOwner()) {
            throw new UnauthorizedOperationException("Only owners can update the company budget");
        }

        Budget budget = budgetRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new BudgetNotFoundException(command.companyId()));

        long previousAmountCents = budget.getAmount().amountCents();
        long currentAllocationsCents = calculateCurrentAllocations(companyId);

        Money newAmount = new Money(command.newAmountCents(), command.currency());
        budget.updateAmount(newAmount);
        budgetRepository.save(budget);

        AuditEvent event = AuditEvent.budgetUpdated(companyId, actorId, budget, previousAmountCents);
        auditEventRepository.save(event);

        boolean isLowerThanAllocations = command.newAmountCents() < currentAllocationsCents;

        return new UpdateBudgetResult(
                budget.getId().toString(),
                newAmount.amountCents(),
                newAmount.currency().name(),
                isLowerThanAllocations,
                currentAllocationsCents
        );
    }

    private long calculateCurrentAllocations(CompanyId companyId) {
        // TODO: Implement when allocations are available (Story 4.x)
        // For now, return 0 as allocations are not yet implemented
        return 0L;
    }
}

