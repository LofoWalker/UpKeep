package com.upkeep.application.port.in;

import com.upkeep.domain.model.membership.Role;

public interface UpdateMemberRoleUseCase {

    UpdateMemberRoleResult execute(UpdateMemberRoleCommand command);

    record UpdateMemberRoleCommand(
            String customerId,
            String companyId,
            String targetMembershipId,
            Role newRole
    ) {}

    record UpdateMemberRoleResult(
            String membershipId,
            Role previousRole,
            Role newRole
    ) {}
}
