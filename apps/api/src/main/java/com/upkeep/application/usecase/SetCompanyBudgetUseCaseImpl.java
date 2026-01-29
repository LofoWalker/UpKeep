package com.upkeep.application.usecase;

import com.upkeep.application.port.in.budget.SetCompanyBudgetUseCase;
import com.upkeep.application.port.out.audit.AuditEventRepository;
import com.upkeep.application.port.out.budget.BudgetRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
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
public class SetCompanyBudgetUseCaseImpl implements SetCompanyBudgetUseCase {

    private final BudgetRepository budgetRepository;
    private final AuditEventRepository auditEventRepository;
    private final MembershipRepository membershipRepository;

    @Inject
    public SetCompanyBudgetUseCaseImpl(BudgetRepository budgetRepository,
                                       AuditEventRepository auditEventRepository,
                                       MembershipRepository membershipRepository) {
        this.budgetRepository = budgetRepository;
        this.auditEventRepository = auditEventRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    @Transactional
    public SetBudgetResult execute(SetBudgetCommand command) {
        CompanyId companyId = CompanyId.from(command.companyId());
        CustomerId actorId = CustomerId.from(command.actorUserId());

        Membership membership = membershipRepository.findByCustomerIdAndCompanyId(actorId, companyId)
                .orElseThrow(() -> new MembershipNotFoundException(command.actorUserId(), command.companyId()));

        if (!membership.isOwner()) {
            throw new UnauthorizedOperationException("Only owners can set the company budget");
        }

        Money amount = new Money(command.amountCents(), command.currency());
        Budget budget = Budget.create(companyId, amount);
        budgetRepository.save(budget);

        AuditEvent event = AuditEvent.budgetCreated(companyId, actorId, budget);
        auditEventRepository.save(event);

        return new SetBudgetResult(
                budget.getId().toString(),
                amount.amountCents(),
                amount.currency().name()
        );
    }
}
