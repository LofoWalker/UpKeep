package com.upkeep.infrastructure.adapter.out.persistence.invitation;

import com.upkeep.domain.model.invitation.InvitationStatus;
import com.upkeep.domain.model.membership.Role;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invitations")
public class InvitationEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "company_id", nullable = false)
    public UUID companyId;

    @Column(name = "invited_by", nullable = false)
    public UUID invitedBy;

    @Column(name = "email", nullable = false)
    public String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    public Role role;

    @Column(name = "token", nullable = false, unique = true)
    public String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public InvitationStatus status;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}
