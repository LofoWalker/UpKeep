package com.upkeep.infrastructure.adapter.out.persistence;

import com.upkeep.application.port.out.UserOAuthProviderRepository;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.oauth.OAuthProvider;
import com.upkeep.domain.model.oauth.UserOAuthProvider;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserOAuthProviderRepositoryImpl implements UserOAuthProviderRepository {

    private final UserOAuthProviderJpaRepository jpaRepository;
    private final UserOAuthProviderMapper mapper;

    public UserOAuthProviderRepositoryImpl(UserOAuthProviderJpaRepository jpaRepository,
                                           UserOAuthProviderMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(UserOAuthProvider userOAuthProvider) {
        UserOAuthProviderEntity entity = mapper.toEntity(userOAuthProvider);
        jpaRepository.persist(entity);
    }

    @Override
    public Optional<UserOAuthProvider> findByProviderAndProviderUserId(OAuthProvider provider,
                                                                        String providerUserId) {
        return jpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<UserOAuthProvider> findByUserIdAndProvider(CustomerId userId, OAuthProvider provider) {
        return jpaRepository.findByUserIdAndProvider(userId.value(), provider)
                .map(mapper::toDomain);
    }
}
