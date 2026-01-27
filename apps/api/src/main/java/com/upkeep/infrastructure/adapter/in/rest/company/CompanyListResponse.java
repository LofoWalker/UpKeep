package com.upkeep.infrastructure.adapter.in.rest.company;

import com.upkeep.domain.model.membership.Role;

public record CompanyListResponse(
        String id,
        String name,
        String slug,
        Role role
) {
}
