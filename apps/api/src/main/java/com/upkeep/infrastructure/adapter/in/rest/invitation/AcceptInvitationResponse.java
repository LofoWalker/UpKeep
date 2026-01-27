package com.upkeep.infrastructure.adapter.in.rest.invitation;

import com.upkeep.domain.model.membership.Role;

public record AcceptInvitationResponse(
        String companyId,
        String companyName,
        String companySlug,
        String membershipId,
        Role role
) {
}
