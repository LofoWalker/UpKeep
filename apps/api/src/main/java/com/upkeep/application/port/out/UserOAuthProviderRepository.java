package com.upkeep.application.port.out;

import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.oauth.OAuthProvider;
import com.upkeep.domain.model.oauth.UserOAuthProvider;

import java.util.Optional;

public interface UserOAuthProviderRepository {

    void save(UserOAuthProvider userOAuthProvider);

    Optional<UserOAuthProvider> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    Optional<UserOAuthProvider> findByUserIdAndProvider(CustomerId userId, OAuthProvider provider);
}
