package com.upkeep.application.usecase;

import com.upkeep.application.port.in.AcceptInvitationUseCase;
import com.upkeep.application.port.out.company.CompanyRepository;
import com.upkeep.application.port.out.invitation.InvitationRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.AlreadyMemberException;
import com.upkeep.domain.exception.CompanyNotFoundException;
import com.upkeep.domain.exception.InvitationExpiredException;
import com.upkeep.domain.exception.InvitationNotFoundException;
import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.invitation.Invitation;
import com.upkeep.domain.model.invitation.InvitationToken;
import com.upkeep.domain.model.membership.Membership;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AcceptInvitationUseCaseImpl implements AcceptInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final MembershipRepository membershipRepository;
    private final CompanyRepository companyRepository;

    @Inject
    public AcceptInvitationUseCaseImpl(InvitationRepository invitationRepository,
                                       MembershipRepository membershipRepository,
                                       CompanyRepository companyRepository) {
        this.invitationRepository = invitationRepository;
        this.membershipRepository = membershipRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public AcceptInvitationResult execute(AcceptInvitationCommand command) {
        InvitationToken token = InvitationToken.from(command.token());
        CustomerId customerId = CustomerId.from(command.customerId());

        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new InvitationNotFoundException(command.token()));

        if (invitation.isExpired()) {
            invitation.markAsExpired();
            invitationRepository.save(invitation);
            throw new InvitationExpiredException();
        }

        if (!invitation.canBeAccepted()) {
            throw new IllegalStateException("Invitation cannot be accepted");
        }

        Company company = companyRepository.findById(invitation.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException(invitation.getCompanyId().toString()));

        if (membershipRepository.existsByCustomerIdAndCompanyId(customerId, invitation.getCompanyId())) {
            invitation.accept();
            invitationRepository.save(invitation);
            throw new AlreadyMemberException();
        }

        invitation.accept();
        invitationRepository.save(invitation);

        Membership membership = Membership.create(customerId, invitation.getCompanyId(), invitation.getRole());
        Membership savedMembership = membershipRepository.save(membership);

        return new AcceptInvitationResult(
                company.getId().toString(),
                company.getName().value(),
                company.getSlug().value(),
                savedMembership.getId().toString(),
                savedMembership.getRole()
        );
    }
}
