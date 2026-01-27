package com.upkeep.application.port.in;

import com.upkeep.domain.model.invitation.InvitationStatus;
import com.upkeep.domain.model.membership.Role;

import java.time.Instant;

public interface GetInvitationUseCase {

    InvitationDetails execute(GetInvitationQuery query);

    record GetInvitationQuery(String token) {}

    record InvitationDetails(
            String invitationId,
            String companyName,
            Role role,
            InvitationStatus status,
            boolean isExpired,
            Instant expiresAt
    ) {}
}
