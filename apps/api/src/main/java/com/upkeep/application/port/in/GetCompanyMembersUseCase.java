package com.upkeep.application.port.in;

import com.upkeep.domain.model.membership.Role;

import java.time.Instant;
import java.util.List;

public interface GetCompanyMembersUseCase {

    List<MemberInfo> execute(GetCompanyMembersQuery query);

    record GetCompanyMembersQuery(
            String customerId,
            String companyId
    ) {}

    record MemberInfo(
            String membershipId,
            String customerId,
            String email,
            Role role,
            Instant joinedAt
    ) {}
}
