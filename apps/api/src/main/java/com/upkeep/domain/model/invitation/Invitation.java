package com.upkeep.domain.model.invitation;

import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.membership.Role;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Invitation {

    private static final int EXPIRATION_DAYS = 7;

    private final InvitationId id;
    private final CompanyId companyId;
    private final CustomerId invitedBy;
    private final Email email;
    private final Role role;
    private final InvitationToken token;
    private InvitationStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Instant updatedAt;

    private Invitation(InvitationId id,
                       CompanyId companyId,
                       CustomerId invitedBy,
                       Email email,
                       Role role,
                       InvitationToken token,
                       InvitationStatus status,
                       Instant createdAt,
                       Instant expiresAt,
                       Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.invitedBy = invitedBy;
        this.email = email;
        this.role = role;
        this.token = token;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.updatedAt = updatedAt;
    }

    public static Invitation create(CompanyId companyId,
                                    CustomerId invitedBy,
                                    Email email,
                                    Role role) {
        Instant now = Instant.now();
        return new Invitation(
                InvitationId.generate(),
                companyId,
                invitedBy,
                email,
                role,
                InvitationToken.generate(),
                InvitationStatus.PENDING,
                now,
                now.plus(EXPIRATION_DAYS, ChronoUnit.DAYS),
                now
        );
    }

    public static Invitation reconstitute(InvitationId id,
                                          CompanyId companyId,
                                          CustomerId invitedBy,
                                          Email email,
                                          Role role,
                                          InvitationToken token,
                                          InvitationStatus status,
                                          Instant createdAt,
                                          Instant expiresAt,
                                          Instant updatedAt) {
        return new Invitation(id, companyId, invitedBy, email, role, token, status, createdAt, expiresAt, updatedAt);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean canBeAccepted() {
        return status == InvitationStatus.PENDING && !isExpired();
    }

    public void accept() {
        if (!canBeAccepted()) {
            throw new IllegalStateException("Invitation cannot be accepted");
        }
        this.status = InvitationStatus.ACCEPTED;
        this.updatedAt = Instant.now();
    }

    public void decline() {
        if (status != InvitationStatus.PENDING) {
            throw new IllegalStateException("Only pending invitations can be declined");
        }
        this.status = InvitationStatus.DECLINED;
        this.updatedAt = Instant.now();
    }

    public void markAsExpired() {
        if (status == InvitationStatus.PENDING) {
            this.status = InvitationStatus.EXPIRED;
            this.updatedAt = Instant.now();
        }
    }

    public InvitationId getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public CustomerId getInvitedBy() {
        return invitedBy;
    }

    public Email getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public InvitationToken getToken() {
        return token;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
