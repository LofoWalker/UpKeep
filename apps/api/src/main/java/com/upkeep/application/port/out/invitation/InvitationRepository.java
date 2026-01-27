package com.upkeep.application.port.out.invitation;

import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.invitation.Invitation;
import com.upkeep.domain.model.invitation.InvitationId;
import com.upkeep.domain.model.invitation.InvitationStatus;
import com.upkeep.domain.model.invitation.InvitationToken;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository {

    Invitation save(Invitation invitation);

    Optional<Invitation> findById(InvitationId id);

    Optional<Invitation> findByToken(InvitationToken token);

    Optional<Invitation> findByCompanyIdAndEmailAndStatus(CompanyId companyId, Email email, InvitationStatus status);

    List<Invitation> findAllByCompanyId(CompanyId companyId);

    List<Invitation> findAllByCompanyIdAndStatus(CompanyId companyId, InvitationStatus status);

    boolean existsByCompanyIdAndEmailAndStatus(CompanyId companyId, Email email, InvitationStatus status);
}
