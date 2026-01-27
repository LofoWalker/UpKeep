package com.upkeep.application.port.in;

import com.upkeep.domain.model.membership.Role;

public interface AcceptInvitationUseCase {

    AcceptInvitationResult execute(AcceptInvitationCommand command);

    record AcceptInvitationCommand(
            String customerId,
            String token
    ) {}

    record AcceptInvitationResult(
            String companyId,
            String companyName,
            String companySlug,
            String membershipId,
            Role role
    ) {}
}
