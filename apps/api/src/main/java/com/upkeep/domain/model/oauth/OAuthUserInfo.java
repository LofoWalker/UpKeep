package com.upkeep.domain.model.oauth;

public record OAuthUserInfo(
        String providerUserId,
        String email,
        String name,
        String avatarUrl
) {
}
