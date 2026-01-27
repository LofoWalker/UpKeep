package com.upkeep.infrastructure.adapter.in.rest.company;

import com.upkeep.domain.model.membership.Role;

import java.time.Instant;

public record MemberResponse(
        String membershipId,
        String customerId,
        String email,
        Role role,
        Instant joinedAt
) {
}
