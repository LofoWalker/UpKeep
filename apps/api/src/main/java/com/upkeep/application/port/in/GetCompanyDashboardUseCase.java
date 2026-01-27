package com.upkeep.application.port.in;

import com.upkeep.domain.model.membership.Role;

public interface GetCompanyDashboardUseCase {

    CompanyDashboard execute(GetCompanyDashboardQuery query);

    record GetCompanyDashboardQuery(
            String customerId,
            String companyId
    ) {}

    record CompanyDashboard(
            String companyId,
            String name,
            String slug,
            Role userRole,
            DashboardStats stats
    ) {}

    record DashboardStats(
            int totalMembers,
            boolean hasBudget,
            boolean hasPackages,
            boolean hasAllocations
    ) {}
}
