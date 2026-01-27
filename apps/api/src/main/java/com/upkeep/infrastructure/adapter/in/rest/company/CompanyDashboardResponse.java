package com.upkeep.infrastructure.adapter.in.rest.company;

import com.upkeep.domain.model.membership.Role;

public record CompanyDashboardResponse(
        String id,
        String name,
        String slug,
        Role userRole,
        StatsResponse stats
) {
    public record StatsResponse(
            int totalMembers,
            boolean hasBudget,
            boolean hasPackages,
            boolean hasAllocations
    ) {
    }
}
