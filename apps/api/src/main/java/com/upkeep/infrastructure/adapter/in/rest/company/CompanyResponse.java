package com.upkeep.infrastructure.adapter.in.rest.company;

import com.upkeep.domain.model.membership.Role;

public record CompanyResponse(
        String id,
        String name,
        String slug,
        MembershipResponse membership
) {
    public record MembershipResponse(
            String id,
            Role role
    ) {
    }
}
