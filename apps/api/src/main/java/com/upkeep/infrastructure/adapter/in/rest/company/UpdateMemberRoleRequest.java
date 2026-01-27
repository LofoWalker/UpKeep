package com.upkeep.infrastructure.adapter.in.rest.company;

import com.upkeep.domain.model.membership.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
        @NotNull(message = "Role is required")
        Role role
) {
}
