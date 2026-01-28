package com.upkeep.infrastructure.adapter.out.security;

import com.upkeep.application.port.out.auth.RefreshTokenRepository;
import com.upkeep.application.port.out.auth.RefreshTokenRepository.RefreshTokenData;
import com.upkeep.application.port.out.auth.TokenService.TokenClaims;
import com.upkeep.application.port.out.customer.CustomerRepository;
import com.upkeep.domain.exception.CustomerNotFoundException;
import com.upkeep.domain.exception.InvalidRefreshTokenException;
import com.upkeep.domain.model.customer.AccountType;
import com.upkeep.domain.model.customer.Customer;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("JwtTokenService")
class JwtTokenServiceTest {

    @Inject
    JwtTokenService tokenService;

    private RefreshTokenRepository refreshTokenRepository;
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        customerRepository = mock(CustomerRepository.class);
    }

    @Nested
    @DisplayName("generateAccessToken")
    class GenerateAccessToken {

        @Test
        @DisplayName("should generate valid JWT for customer")
        void shouldGenerateValidJwt() {
            Customer customer = createTestCustomer();

            String token = tokenService.generateAccessToken(customer);

            assertNotNull(token);
            assertFalse(token.isBlank());
        }
    }

    @Nested
    @DisplayName("generateRefreshToken")
    class GenerateRefreshToken {

        @Test
        @DisplayName("should generate refresh token and save to repository")
        void shouldGenerateAndSaveRefreshToken() {
            JwtTokenService service = new JwtTokenService(refreshTokenRepository, customerRepository, null);
            Customer customer = createTestCustomer();

            String token = service.generateRefreshToken(customer);

            assertNotNull(token);
            assertFalse(token.isBlank());
            verify(refreshTokenRepository).save(any(RefreshTokenData.class));
        }
    }

    @Nested
    @DisplayName("validateAccessToken")
    class ValidateAccessToken {

        @Test
        @DisplayName("should validate and extract claims from valid token")
        void shouldValidateAndExtractClaims() {
            Customer customer = createTestCustomer();
            String token = tokenService.generateAccessToken(customer);

            TokenClaims claims = tokenService.validateAccessToken(token);

            assertNotNull(claims);
            assertEquals(customer.getId().value().toString(), claims.userId());
            assertEquals(customer.getEmail().value(), claims.email());
            assertEquals(customer.getAccountType().name(), claims.accountType());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for invalid token")
        void shouldThrowForInvalidToken() {
            assertThrows(IllegalArgumentException.class,
                    () -> tokenService.validateAccessToken("invalid-token"));
        }
    }

    @Nested
    @DisplayName("refreshAccessToken")
    class RefreshAccessToken {

        @Test
        @DisplayName("should throw InvalidRefreshTokenException when token not found")
        void shouldThrowWhenTokenNotFound() {
            JwtTokenService service = new JwtTokenService(refreshTokenRepository, customerRepository, null);
            when(refreshTokenRepository.findByToken("invalid-token"))
                    .thenReturn(Optional.empty());

            assertThrows(InvalidRefreshTokenException.class,
                    () -> service.refreshAccessToken("invalid-token"));
        }

        @Test
        @DisplayName("should throw InvalidRefreshTokenException when token is expired")
        void shouldThrowWhenTokenExpired() {
            JwtTokenService service = new JwtTokenService(refreshTokenRepository, customerRepository, null);
            RefreshTokenData expiredToken = createRefreshTokenData(true, false);
            when(refreshTokenRepository.findByToken("expired-token"))
                    .thenReturn(Optional.of(expiredToken));

            assertThrows(InvalidRefreshTokenException.class,
                    () -> service.refreshAccessToken("expired-token"));
        }

        @Test
        @DisplayName("should throw InvalidRefreshTokenException when token is revoked")
        void shouldThrowWhenTokenRevoked() {
            JwtTokenService service = new JwtTokenService(refreshTokenRepository, customerRepository, null);
            RefreshTokenData revokedToken = createRefreshTokenData(false, true);
            when(refreshTokenRepository.findByToken("revoked-token"))
                    .thenReturn(Optional.of(revokedToken));

            assertThrows(InvalidRefreshTokenException.class,
                    () -> service.refreshAccessToken("revoked-token"));
        }

        @Test
        @DisplayName("should throw CustomerNotFoundException when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            JwtTokenService service = new JwtTokenService(refreshTokenRepository, customerRepository, null);
            CustomerId customerId = CustomerId.generate();
            RefreshTokenData validToken = createRefreshTokenData(customerId, false, false);
            when(refreshTokenRepository.findByToken("valid-token"))
                    .thenReturn(Optional.of(validToken));
            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.empty());

            assertThrows(CustomerNotFoundException.class,
                    () -> service.refreshAccessToken("valid-token"));
        }
    }

    @Nested
    @DisplayName("revokeRefreshToken")
    class RevokeRefreshToken {

        @Test
        @DisplayName("should call repository revokeByToken")
        void shouldCallRepositoryRevokeByToken() {
            JwtTokenService service = new JwtTokenService(refreshTokenRepository, customerRepository, null);
            String token = "some-token";

            service.revokeRefreshToken(token);

            verify(refreshTokenRepository).revokeByToken(token);
        }
    }

    private Customer createTestCustomer() {
        return Customer.reconstitute(
                CustomerId.generate(),
                new Email("test@example.com"),
                null,
                AccountType.COMPANY,
                Instant.now(),
                Instant.now()
        );
    }

    private RefreshTokenData createRefreshTokenData(boolean expired, boolean revoked) {
        return createRefreshTokenData(CustomerId.generate(), expired, revoked);
    }

    private RefreshTokenData createRefreshTokenData(CustomerId customerId, boolean expired, boolean revoked) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = expired ? Instant.now().minusSeconds(600) : Instant.now().plusSeconds(600);
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant revokedAt = revoked ? Instant.now() : null;

        return new RefreshTokenData(
                token,
                customerId,
                expiresAt,
                createdAt,
                revokedAt
        );
    }
}
