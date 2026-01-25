package com.upkeep.infrastructure.adapter.out.security;

import com.upkeep.application.port.out.auth.RefreshTokenRepository;
import com.upkeep.application.port.out.auth.RefreshTokenRepository.RefreshTokenData;
import com.upkeep.application.port.out.auth.TokenService;
import com.upkeep.application.port.out.customer.CustomerRepository;
import com.upkeep.domain.exception.CustomerNotFoundException;
import com.upkeep.domain.exception.InvalidRefreshTokenException;
import com.upkeep.domain.model.customer.Customer;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class JwtTokenService implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomerRepository customerRepository;

    @ConfigProperty(name = "jwt.access-token-expiry-seconds", defaultValue = "900")
    int accessTokenExpirySeconds;

    @ConfigProperty(name = "jwt.refresh-token-expiry-seconds", defaultValue = "604800")
    int refreshTokenExpirySeconds;

    public JwtTokenService(RefreshTokenRepository refreshTokenRepository,
                           CustomerRepository customerRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public String generateAccessToken(Customer customer) {
        return Jwt.issuer("upkeep")
                .subject(customer.getId().value().toString())
                .claim("email", customer.getEmail().value())
                .claim("accountType", customer.getAccountType().name())
                .expiresIn(Duration.ofSeconds(accessTokenExpirySeconds))
                .sign();
    }

    @Override
    @Transactional
    public String generateRefreshToken(Customer customer) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(refreshTokenExpirySeconds);

        RefreshTokenData tokenData = RefreshTokenData.create(
                token,
                customer.getId(),
                expiresAt
        );

        refreshTokenRepository.save(tokenData);
        return token;
    }

    @Override
    public TokenClaims validateAccessToken(String token) {
        throw new UnsupportedOperationException("Use Quarkus JWT security for token validation");
    }

    @Override
    @Transactional
    public RefreshResult refreshAccessToken(String refreshToken) {
        RefreshTokenData tokenData = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(InvalidRefreshTokenException::notFound);

        if (tokenData.isExpired()) {
            throw InvalidRefreshTokenException.expired();
        }
        if (tokenData.isRevoked()) {
            throw InvalidRefreshTokenException.revoked();
        }

        Customer customer = customerRepository.findById(tokenData.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(tokenData.customerId().toString()));

        String newAccessToken = generateAccessToken(customer);

        return new RefreshResult(newAccessToken, customer.getId().value().toString());
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        refreshTokenRepository.revokeByToken(refreshToken);
    }
}
