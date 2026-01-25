package com.upkeep.domain.model.oauth;

import com.upkeep.domain.model.customer.CustomerId;

import java.time.Instant;
import java.util.UUID;

public class UserOAuthProvider {
    private final UUID id;
    private final CustomerId userId;
    private final OAuthProvider provider;
    private final String providerUserId;
    private final String providerEmail;
    private final Instant createdAt;

    private UserOAuthProvider(UUID id,
                              CustomerId userId,
                              OAuthProvider provider,
                              String providerUserId,
                              String providerEmail,
                              Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.providerEmail = providerEmail;
        this.createdAt = createdAt;
    }

    public static UserOAuthProvider create(CustomerId userId,
                                           OAuthProvider provider,
                                           String providerUserId,
                                           String providerEmail) {
        return new UserOAuthProvider(
                UUID.randomUUID(),
                userId,
                provider,
                providerUserId,
                providerEmail,
                Instant.now()
        );
    }

    public static UserOAuthProvider reconstitute(UUID id,
                                                 CustomerId userId,
                                                 OAuthProvider provider,
                                                 String providerUserId,
                                                 String providerEmail,
                                                 Instant createdAt) {
        return new UserOAuthProvider(id, userId, provider, providerUserId, providerEmail, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public CustomerId getUserId() {
        return userId;
    }

    public OAuthProvider getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public String getProviderEmail() {
        return providerEmail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
