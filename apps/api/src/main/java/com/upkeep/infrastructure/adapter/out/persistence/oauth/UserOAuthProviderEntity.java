package com.upkeep.infrastructure.adapter.out.persistence.oauth;

import com.upkeep.domain.model.oauth.OAuthProvider;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_oauth_providers")
public class UserOAuthProviderEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    public OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    public String providerUserId;

    @Column(name = "provider_email")
    public String providerEmail;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
