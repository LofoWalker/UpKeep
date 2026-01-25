package com.upkeep.infrastructure.adapter.out.persistence.auth;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity extends PanacheEntityBase {

    @Id
    @Column(name = "token", nullable = false)
    public String token;

    @Column(name = "customer_id", nullable = false)
    public UUID customerId;

    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "revoked_at")
    public Instant revokedAt;
}
