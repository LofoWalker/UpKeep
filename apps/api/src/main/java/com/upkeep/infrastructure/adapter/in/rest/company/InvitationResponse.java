package com.upkeep.infrastructure.adapter.in.rest.company;

import com.upkeep.domain.model.invitation.InvitationStatus;
import com.upkeep.domain.model.membership.Role;

import java.time.Instant;

public record InvitationResponse(
        String id,
        String email,
        Role role,
        InvitationStatus status,
        Instant expiresAt
) {
}
