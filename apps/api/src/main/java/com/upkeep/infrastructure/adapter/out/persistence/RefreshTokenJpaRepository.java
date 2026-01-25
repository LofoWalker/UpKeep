package com.upkeep.infrastructure.adapter.out.persistence;

import com.upkeep.application.port.out.RefreshTokenRepository;
import com.upkeep.domain.model.customer.CustomerId;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class RefreshTokenJpaRepository implements RefreshTokenRepository {

    @Override
    public void save(RefreshTokenData tokenData) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.token = tokenData.token();
        entity.customerId = tokenData.customerId().value();
        entity.expiresAt = tokenData.expiresAt();
        entity.createdAt = tokenData.createdAt();
        entity.revokedAt = tokenData.revokedAt();
        entity.persist();
    }

    @Override
    public Optional<RefreshTokenData> findByToken(String token) {
        return RefreshTokenEntity.<RefreshTokenEntity>findByIdOptional(token)
                .map(this::toData);
    }

    @Override
    public void revokeByToken(String token) {
        RefreshTokenEntity.<RefreshTokenEntity>findByIdOptional(token)
                .ifPresent(entity -> entity.revokedAt = Instant.now());
    }

    @Override
    public void revokeAllByCustomerId(CustomerId customerId) {
        RefreshTokenEntity.update(
                "revokedAt = ?1 WHERE customerId = ?2 AND revokedAt IS NULL",
                Instant.now(),
                customerId.value()
        );
    }

    @Override
    public void deleteExpiredTokens() {
        RefreshTokenEntity.delete("expiresAt < ?1", Instant.now());
    }

    private RefreshTokenData toData(RefreshTokenEntity entity) {
        return new RefreshTokenData(
                entity.token,
                CustomerId.from(entity.customerId),
                entity.expiresAt,
                entity.createdAt,
                entity.revokedAt
        );
    }
}
