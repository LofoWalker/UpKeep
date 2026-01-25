package com.upkeep.application.port.out;

import com.upkeep.domain.model.oauth.OAuthProvider;
import com.upkeep.domain.model.oauth.OAuthUserInfo;

public interface OAuthProviderAdapter {

    OAuthProvider getProvider();

    String getAuthorizationUrl(String state);

    OAuthTokenResponse exchangeCode(String code);

    OAuthUserInfo getUserInfo(String accessToken);

    record OAuthTokenResponse(
            String accessToken,
            String tokenType,
            String scope
    ) {
    }
}
