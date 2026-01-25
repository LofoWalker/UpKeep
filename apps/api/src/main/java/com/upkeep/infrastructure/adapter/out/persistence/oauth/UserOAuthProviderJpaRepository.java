package com.upkeep.infrastructure.adapter.out.persistence.oauth;

import com.upkeep.domain.model.oauth.OAuthProvider;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserOAuthProviderJpaRepository implements PanacheRepositoryBase<UserOAuthProviderEntity, UUID> {

    public Optional<UserOAuthProviderEntity> findByProviderAndProviderUserId(OAuthProvider provider,
                                                                              String providerUserId) {
        return find("provider = ?1 and providerUserId = ?2", provider, providerUserId)
                .firstResultOptional();
    }

    public Optional<UserOAuthProviderEntity> findByUserIdAndProvider(UUID userId, OAuthProvider provider) {
        return find("userId = ?1 and provider = ?2", userId, provider)
                .firstResultOptional();
    }
}
