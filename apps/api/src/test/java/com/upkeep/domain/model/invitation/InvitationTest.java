package com.upkeep.domain.model.invitation;

import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.membership.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Invitation")
class InvitationTest {

    @Test
    @DisplayName("should create invitation with pending status and 7 days expiration")
    void shouldCreateInvitation() {
        CompanyId companyId = CompanyId.generate();
        CustomerId invitedBy = CustomerId.generate();
        Email email = new Email("invitee@test.com");
        Role role = Role.MEMBER;

        Instant before = Instant.now();
        Invitation invitation = Invitation.create(companyId, invitedBy, email, role);
        Instant after = Instant.now();

        assertNotNull(invitation.getId());
        assertEquals(companyId, invitation.getCompanyId());
        assertEquals(invitedBy, invitation.getInvitedBy());
        assertEquals(email, invitation.getEmail());
        assertEquals(role, invitation.getRole());
        assertNotNull(invitation.getToken());
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
        assertTrue(invitation.getCreatedAt().compareTo(before) >= 0);
        assertTrue(invitation.getCreatedAt().compareTo(after) <= 0);
        assertTrue(invitation.getExpiresAt().isAfter(invitation.getCreatedAt().plus(6, ChronoUnit.DAYS)));
    }

    @Test
    @DisplayName("should reconstitute invitation from persisted data")
    void shouldReconstituteInvitation() {
        InvitationId id = InvitationId.generate();
        CompanyId companyId = CompanyId.generate();
        CustomerId invitedBy = CustomerId.generate();
        Email email = new Email("invitee@test.com");
        Role role = Role.OWNER;
        InvitationToken token = InvitationToken.generate();
        InvitationStatus status = InvitationStatus.ACCEPTED;
        Instant createdAt = Instant.now().minus(3, ChronoUnit.DAYS);
        Instant expiresAt = Instant.now().plus(4, ChronoUnit.DAYS);
        Instant updatedAt = Instant.now();

        Invitation invitation = Invitation.reconstitute(
                id, companyId, invitedBy, email, role, token, status, createdAt, expiresAt, updatedAt
        );

        assertEquals(id, invitation.getId());
        assertEquals(companyId, invitation.getCompanyId());
        assertEquals(invitedBy, invitation.getInvitedBy());
        assertEquals(email, invitation.getEmail());
        assertEquals(role, invitation.getRole());
        assertEquals(token, invitation.getToken());
        assertEquals(status, invitation.getStatus());
        assertEquals(createdAt, invitation.getCreatedAt());
        assertEquals(expiresAt, invitation.getExpiresAt());
        assertEquals(updatedAt, invitation.getUpdatedAt());
    }

    @Test
    @DisplayName("should return false for isExpired when invitation is still valid")
    void shouldReturnFalseForIsExpiredWhenValid() {
        Invitation invitation = Invitation.create(
                CompanyId.generate(), CustomerId.generate(), new Email("test@test.com"), Role.MEMBER
        );

        assertFalse(invitation.isExpired());
    }

    @Test
    @DisplayName("should return true for isExpired when invitation has expired")
    void shouldReturnTrueForIsExpiredWhenExpired() {
        Invitation invitation = Invitation.reconstitute(
                InvitationId.generate(),
                CompanyId.generate(),
                CustomerId.generate(),
                new Email("test@test.com"),
                Role.MEMBER,
                InvitationToken.generate(),
                InvitationStatus.PENDING,
                Instant.now().minus(10, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                Instant.now().minus(10, ChronoUnit.DAYS)
        );

        assertTrue(invitation.isExpired());
    }

    @Test
    @DisplayName("should return true for canBeAccepted when pending and not expired")
    void shouldReturnTrueForCanBeAcceptedWhenValid() {
        Invitation invitation = Invitation.create(
                CompanyId.generate(), CustomerId.generate(), new Email("test@test.com"), Role.MEMBER
        );

        assertTrue(invitation.canBeAccepted());
    }

    @Test
    @DisplayName("should return false for canBeAccepted when already accepted")
    void shouldReturnFalseForCanBeAcceptedWhenAccepted() {
        Invitation invitation = Invitation.reconstitute(
                InvitationId.generate(),
                CompanyId.generate(),
                CustomerId.generate(),
                new Email("test@test.com"),
                Role.MEMBER,
                InvitationToken.generate(),
                InvitationStatus.ACCEPTED,
                Instant.now(),
                Instant.now().plus(7, ChronoUnit.DAYS),
                Instant.now()
        );

        assertFalse(invitation.canBeAccepted());
    }

    @Test
    @DisplayName("should accept invitation and update status")
    void shouldAcceptInvitation() {
        Invitation invitation = Invitation.create(
                CompanyId.generate(), CustomerId.generate(), new Email("test@test.com"), Role.MEMBER
        );
        Instant beforeAccept = Instant.now();

        invitation.accept();

        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        assertTrue(invitation.getUpdatedAt().compareTo(beforeAccept) >= 0);
    }

    @Test
    @DisplayName("should throw IllegalStateException when accepting non-pending invitation")
    void shouldThrowWhenAcceptingNonPending() {
        Invitation invitation = Invitation.reconstitute(
                InvitationId.generate(),
                CompanyId.generate(),
                CustomerId.generate(),
                new Email("test@test.com"),
                Role.MEMBER,
                InvitationToken.generate(),
                InvitationStatus.ACCEPTED,
                Instant.now(),
                Instant.now().plus(7, ChronoUnit.DAYS),
                Instant.now()
        );

        assertThrows(IllegalStateException.class, () -> invitation.accept());
    }

    @Test
    @DisplayName("should decline invitation and update status")
    void shouldDeclineInvitation() {
        Invitation invitation = Invitation.create(
                CompanyId.generate(), CustomerId.generate(), new Email("test@test.com"), Role.MEMBER
        );

        invitation.decline();

        assertEquals(InvitationStatus.DECLINED, invitation.getStatus());
    }

    @Test
    @DisplayName("should throw IllegalStateException when declining non-pending invitation")
    void shouldThrowWhenDecliningNonPending() {
        Invitation invitation = Invitation.reconstitute(
                InvitationId.generate(),
                CompanyId.generate(),
                CustomerId.generate(),
                new Email("test@test.com"),
                Role.MEMBER,
                InvitationToken.generate(),
                InvitationStatus.ACCEPTED,
                Instant.now(),
                Instant.now().plus(7, ChronoUnit.DAYS),
                Instant.now()
        );

        assertThrows(IllegalStateException.class, () -> invitation.decline());
    }

    @Test
    @DisplayName("should mark as expired when pending")
    void shouldMarkAsExpired() {
        Invitation invitation = Invitation.create(
                CompanyId.generate(), CustomerId.generate(), new Email("test@test.com"), Role.MEMBER
        );

        invitation.markAsExpired();

        assertEquals(InvitationStatus.EXPIRED, invitation.getStatus());
    }

    @Test
    @DisplayName("should not change status when marking already accepted invitation as expired")
    void shouldNotMarkAsExpiredWhenAlreadyAccepted() {
        Invitation invitation = Invitation.reconstitute(
                InvitationId.generate(),
                CompanyId.generate(),
                CustomerId.generate(),
                new Email("test@test.com"),
                Role.MEMBER,
                InvitationToken.generate(),
                InvitationStatus.ACCEPTED,
                Instant.now(),
                Instant.now().plus(7, ChronoUnit.DAYS),
                Instant.now()
        );

        invitation.markAsExpired();

        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
    }
}
