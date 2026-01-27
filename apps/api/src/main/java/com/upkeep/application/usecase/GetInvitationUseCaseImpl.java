package com.upkeep.application.usecase;

import com.upkeep.application.port.in.GetInvitationUseCase;
import com.upkeep.application.port.out.company.CompanyRepository;
import com.upkeep.application.port.out.invitation.InvitationRepository;
import com.upkeep.domain.exception.CompanyNotFoundException;
import com.upkeep.domain.exception.InvitationNotFoundException;
import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.invitation.Invitation;
import com.upkeep.domain.model.invitation.InvitationToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetInvitationUseCaseImpl implements GetInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final CompanyRepository companyRepository;

    @Inject
    public GetInvitationUseCaseImpl(InvitationRepository invitationRepository,
                                    CompanyRepository companyRepository) {
        this.invitationRepository = invitationRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public InvitationDetails execute(GetInvitationQuery query) {
        InvitationToken token = InvitationToken.from(query.token());

        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new InvitationNotFoundException(query.token()));

        Company company = companyRepository.findById(invitation.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException(invitation.getCompanyId().toString()));

        return new InvitationDetails(
                invitation.getId().toString(),
                company.getName().value(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.isExpired(),
                invitation.getExpiresAt()
        );
    }
}
