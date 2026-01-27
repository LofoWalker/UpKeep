package com.upkeep.application.usecase;

import com.upkeep.application.port.in.InviteUserToCompanyUseCase;
import com.upkeep.application.port.out.invitation.InvitationRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.application.port.out.notification.EmailService;
import com.upkeep.domain.exception.InvitationAlreadyExistsException;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.exception.UnauthorizedOperationException;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.invitation.Invitation;
import com.upkeep.domain.model.invitation.InvitationStatus;
import com.upkeep.domain.model.membership.Membership;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class InviteUserToCompanyUseCaseImpl implements InviteUserToCompanyUseCase {

    private final MembershipRepository membershipRepository;
    private final InvitationRepository invitationRepository;
    private final EmailService emailService;

    @Inject
    public InviteUserToCompanyUseCaseImpl(MembershipRepository membershipRepository,
                                          InvitationRepository invitationRepository,
                                          EmailService emailService) {
        this.membershipRepository = membershipRepository;
        this.invitationRepository = invitationRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public InviteResult execute(InviteCommand command) {
        CustomerId customerId = CustomerId.from(command.customerId());
        CompanyId companyId = CompanyId.from(command.companyId());
        Email inviteeEmail = new Email(command.email());

        Membership inviterMembership = membershipRepository.findByCustomerIdAndCompanyId(customerId, companyId)
                .orElseThrow(() -> new MembershipNotFoundException(command.customerId(), command.companyId()));

        if (!inviterMembership.isOwner()) {
            throw new UnauthorizedOperationException("Only owners can invite members");
        }

        boolean pendingInvitationExists = invitationRepository
                .existsByCompanyIdAndEmailAndStatus(companyId, inviteeEmail, InvitationStatus.PENDING);
        if (pendingInvitationExists) {
            throw new InvitationAlreadyExistsException(command.email());
        }

        Invitation invitation = Invitation.create(companyId, customerId, inviteeEmail, command.role());
        Invitation savedInvitation = invitationRepository.save(invitation);

        emailService.sendInvitationEmail(inviteeEmail, savedInvitation.getToken().value());

        return new InviteResult(
                savedInvitation.getId().toString(),
                savedInvitation.getEmail().value(),
                savedInvitation.getRole(),
                savedInvitation.getStatus(),
                savedInvitation.getExpiresAt()
        );
    }
}
