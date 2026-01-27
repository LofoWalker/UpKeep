package com.upkeep.domain.model.membership;

import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Membership")
class MembershipTest {

    @Test
    @DisplayName("should create membership with generated ID and timestamps")
    void shouldCreateMembership() {
        CustomerId customerId = CustomerId.generate();
        CompanyId companyId = CompanyId.generate();
        Role role = Role.MEMBER;

        Instant before = Instant.now();
        Membership membership = Membership.create(customerId, companyId, role);
        Instant after = Instant.now();

        assertNotNull(membership.getId());
        assertEquals(customerId, membership.getCustomerId());
        assertEquals(companyId, membership.getCompanyId());
        assertEquals(role, membership.getRole());
        assertTrue(membership.getJoinedAt().compareTo(before) >= 0);
        assertTrue(membership.getJoinedAt().compareTo(after) <= 0);
        assertEquals(membership.getJoinedAt(), membership.getUpdatedAt());
    }

    @Test
    @DisplayName("should reconstitute membership from persisted data")
    void shouldReconstituteMembership() {
        MembershipId id = MembershipId.generate();
        CustomerId customerId = CustomerId.generate();
        CompanyId companyId = CompanyId.generate();
        Role role = Role.OWNER;
        Instant joinedAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now();

        Membership membership = Membership.reconstitute(id, customerId, companyId, role, joinedAt, updatedAt);

        assertEquals(id, membership.getId());
        assertEquals(customerId, membership.getCustomerId());
        assertEquals(companyId, membership.getCompanyId());
        assertEquals(role, membership.getRole());
        assertEquals(joinedAt, membership.getJoinedAt());
        assertEquals(updatedAt, membership.getUpdatedAt());
    }

    @Test
    @DisplayName("should change role and update timestamp")
    void shouldChangeRole() {
        CustomerId customerId = CustomerId.generate();
        CompanyId companyId = CompanyId.generate();
        Membership membership = Membership.create(customerId, companyId, Role.MEMBER);

        Instant updatedAtBefore = membership.getUpdatedAt();

        membership.changeRole(Role.OWNER);

        assertEquals(Role.OWNER, membership.getRole());
        assertTrue(membership.getUpdatedAt().compareTo(updatedAtBefore) >= 0);
    }

    @Test
    @DisplayName("should return true for isOwner when role is OWNER")
    void shouldReturnTrueForIsOwnerWhenOwner() {
        Membership membership = Membership.create(CustomerId.generate(), CompanyId.generate(), Role.OWNER);

        assertTrue(membership.isOwner());
    }

    @Test
    @DisplayName("should return false for isOwner when role is MEMBER")
    void shouldReturnFalseForIsOwnerWhenMember() {
        Membership membership = Membership.create(CustomerId.generate(), CompanyId.generate(), Role.MEMBER);

        assertFalse(membership.isOwner());
    }
}
