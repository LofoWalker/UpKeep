package com.upkeep.infrastructure.adapter.in.rest.invitation;

import com.upkeep.domain.model.invitation.InvitationStatus;
import com.upkeep.domain.model.membership.Role;

import java.time.Instant;

public record InvitationDetailsResponse(
        String id,
        String companyName,
        Role role,
        InvitationStatus status,
        boolean isExpired,
        Instant expiresAt
) {
}
