package com.upkeep.application.port.in;

import com.upkeep.domain.model.membership.Role;

public interface CreateCompanyUseCase {

    CreateCompanyResult execute(CreateCompanyCommand command);

    record CreateCompanyCommand(
            String customerId,
            String name,
            String slug
    ) {}

    record CreateCompanyResult(
            String companyId,
            String name,
            String slug,
            MembershipInfo membership
    ) {}

    record MembershipInfo(
            String membershipId,
            Role role
    ) {}
}
