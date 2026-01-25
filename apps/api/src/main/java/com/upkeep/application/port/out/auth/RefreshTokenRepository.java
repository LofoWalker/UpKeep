package com.upkeep.application.port.out.auth;

import com.upkeep.domain.model.customer.CustomerId;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {
    void save(RefreshTokenData tokenData);

    Optional<RefreshTokenData> findByToken(String token);

    void revokeByToken(String token);

    void revokeAllByCustomerId(CustomerId customerId);

    void deleteExpiredTokens();

    record RefreshTokenData(
            String token,
            CustomerId customerId,
            Instant expiresAt,
            Instant createdAt,
            Instant revokedAt
    ) {
        public static RefreshTokenData create(String token, CustomerId customerId, Instant expiresAt) {
            return new RefreshTokenData(token, customerId, expiresAt, Instant.now(), null);
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }

        public boolean isRevoked() {
            return revokedAt != null;
        }

        public boolean isValid() {
            return !isExpired() && !isRevoked();
        }
    }
}
