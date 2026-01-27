package com.upkeep.application.port.in;

import com.upkeep.domain.model.membership.Role;

import java.util.List;

public interface GetUserCompaniesUseCase {

    List<CompanyWithMembership> execute(GetUserCompaniesQuery query);

    record GetUserCompaniesQuery(String customerId) {}

    record CompanyWithMembership(
            String companyId,
            String name,
            String slug,
            Role role
    ) {}
}
