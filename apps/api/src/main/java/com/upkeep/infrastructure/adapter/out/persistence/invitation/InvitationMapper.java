package com.upkeep.infrastructure.adapter.out.persistence.invitation;

import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.invitation.Invitation;
import com.upkeep.domain.model.invitation.InvitationId;
import com.upkeep.domain.model.invitation.InvitationToken;

public final class InvitationMapper {

    private InvitationMapper() {
    }

    public static InvitationEntity toEntity(Invitation invitation) {
        InvitationEntity entity = new InvitationEntity();
        entity.id = invitation.getId().value();
        entity.companyId = invitation.getCompanyId().value();
        entity.invitedBy = invitation.getInvitedBy().value();
        entity.email = invitation.getEmail().value();
        entity.role = invitation.getRole();
        entity.token = invitation.getToken().value();
        entity.status = invitation.getStatus();
        entity.createdAt = invitation.getCreatedAt();
        entity.expiresAt = invitation.getExpiresAt();
        entity.updatedAt = invitation.getUpdatedAt();
        return entity;
    }

    public static Invitation toDomain(InvitationEntity entity) {
        return Invitation.reconstitute(
                InvitationId.from(entity.id),
                CompanyId.from(entity.companyId),
                CustomerId.from(entity.invitedBy),
                new Email(entity.email),
                entity.role,
                InvitationToken.from(entity.token),
                entity.status,
                entity.createdAt,
                entity.expiresAt,
                entity.updatedAt
        );
    }
}
