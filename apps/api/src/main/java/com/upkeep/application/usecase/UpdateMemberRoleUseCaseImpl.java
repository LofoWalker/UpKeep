package com.upkeep.application.usecase;

import com.upkeep.application.port.in.UpdateMemberRoleUseCase;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.LastOwnerException;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.exception.UnauthorizedOperationException;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.MembershipId;
import com.upkeep.domain.model.membership.Role;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UpdateMemberRoleUseCaseImpl implements UpdateMemberRoleUseCase {

    private final MembershipRepository membershipRepository;

    @Inject
    public UpdateMemberRoleUseCaseImpl(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Override
    @Transactional
    public UpdateMemberRoleResult execute(UpdateMemberRoleCommand command) {
        CustomerId customerId = CustomerId.from(command.customerId());
        CompanyId companyId = CompanyId.from(command.companyId());
        MembershipId targetMembershipId = MembershipId.from(command.targetMembershipId());

        Membership requesterMembership = membershipRepository.findByCustomerIdAndCompanyId(customerId, companyId)
                .orElseThrow(() -> new MembershipNotFoundException(command.customerId(), command.companyId()));

        if (!requesterMembership.isOwner()) {
            throw new UnauthorizedOperationException("Only owners can change member roles");
        }

        Membership targetMembership = membershipRepository.findById(targetMembershipId)
                .orElseThrow(() -> new MembershipNotFoundException(command.targetMembershipId(), command.companyId()));

        if (!targetMembership.getCompanyId().equals(companyId)) {
            throw new MembershipNotFoundException(command.targetMembershipId(), command.companyId());
        }

        Role previousRole = targetMembership.getRole();

        if (previousRole == Role.OWNER && command.newRole() == Role.MEMBER) {
            long ownerCount = membershipRepository.countByCompanyIdAndRole(companyId, Role.OWNER);
            if (ownerCount <= 1) {
                throw new LastOwnerException();
            }
        }

        targetMembership.changeRole(command.newRole());
        membershipRepository.save(targetMembership);

        return new UpdateMemberRoleResult(
                targetMembership.getId().toString(),
                previousRole,
                command.newRole()
        );
    }
}
