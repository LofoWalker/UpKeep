package com.upkeep.domain.model.invitation;

import java.security.SecureRandom;
import java.util.Base64;

public record InvitationToken(String value) {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;

    public InvitationToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invitation token cannot be empty");
        }
    }

    public static InvitationToken generate() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new InvitationToken(token);
    }

    public static InvitationToken from(String value) {
        return new InvitationToken(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
