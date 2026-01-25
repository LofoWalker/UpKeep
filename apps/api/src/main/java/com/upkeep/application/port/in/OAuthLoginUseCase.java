package com.upkeep.application.port.in;

import com.upkeep.domain.model.customer.AccountType;
import com.upkeep.domain.model.oauth.OAuthProvider;

public interface OAuthLoginUseCase {

    OAuthResult execute(OAuthCommand command);

    record OAuthCommand(
            OAuthProvider provider,
            String providerUserId,
            String email,
            AccountType accountType
    ) {
    }

    record OAuthResult(
            String accessToken,
            String refreshToken,
            boolean isNewUser,
            String userId,
            String email,
            AccountType accountType
    ) {
    }
}
