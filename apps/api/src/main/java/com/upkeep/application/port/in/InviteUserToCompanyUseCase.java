package com.upkeep.application.port.in;

import com.upkeep.domain.model.invitation.InvitationStatus;
import com.upkeep.domain.model.membership.Role;

import java.time.Instant;

public interface InviteUserToCompanyUseCase {

    InviteResult execute(InviteCommand command);

    record InviteCommand(
            String customerId,
            String companyId,
            String email,
            Role role
    ) {}

    record InviteResult(
            String invitationId,
            String email,
            Role role,
            InvitationStatus status,
            Instant expiresAt
    ) {}
}
