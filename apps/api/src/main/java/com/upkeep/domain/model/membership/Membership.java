package com.upkeep.domain.model.membership;

import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;

import java.time.Instant;

public class Membership {
    private final MembershipId id;
    private final CustomerId customerId;
    private final CompanyId companyId;
    private Role role;
    private final Instant joinedAt;
    private Instant updatedAt;

    private Membership(MembershipId id,
                       CustomerId customerId,
                       CompanyId companyId,
                       Role role,
                       Instant joinedAt,
                       Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.companyId = companyId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.updatedAt = updatedAt;
    }

    public static Membership create(CustomerId customerId, CompanyId companyId, Role role) {
        Instant now = Instant.now();
        return new Membership(
                MembershipId.generate(),
                customerId,
                companyId,
                role,
                now,
                now
        );
    }

    public static Membership reconstitute(MembershipId id,
                                          CustomerId customerId,
                                          CompanyId companyId,
                                          Role role,
                                          Instant joinedAt,
                                          Instant updatedAt) {
        return new Membership(id, customerId, companyId, role, joinedAt, updatedAt);
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
        this.updatedAt = Instant.now();
    }

    public boolean isOwner() {
        return this.role == Role.OWNER;
    }

    public MembershipId getId() {
        return id;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public Role getRole() {
        return role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
