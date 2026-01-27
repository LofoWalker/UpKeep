package com.upkeep.infrastructure.adapter.out.persistence.membership;

import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.MembershipId;

public final class MembershipMapper {

    private MembershipMapper() {
    }

    public static MembershipEntity toEntity(Membership membership) {
        MembershipEntity entity = new MembershipEntity();
        entity.id = membership.getId().value();
        entity.customerId = membership.getCustomerId().value();
        entity.companyId = membership.getCompanyId().value();
        entity.role = membership.getRole();
        entity.joinedAt = membership.getJoinedAt();
        entity.updatedAt = membership.getUpdatedAt();
        return entity;
    }

    public static Membership toDomain(MembershipEntity entity) {
        return Membership.reconstitute(
                MembershipId.from(entity.id),
                CustomerId.from(entity.customerId),
                CompanyId.from(entity.companyId),
                entity.role,
                entity.joinedAt,
                entity.updatedAt
        );
    }
}
