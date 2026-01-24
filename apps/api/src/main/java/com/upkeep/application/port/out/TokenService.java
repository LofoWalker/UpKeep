package com.upkeep.application.port.out;

import com.upkeep.domain.model.customer.Customer;

public interface TokenService {
    String generateAccessToken(Customer customer);

    String generateRefreshToken(Customer customer);

    TokenClaims validateAccessToken(String token);

    RefreshResult refreshAccessToken(String refreshToken);

    void revokeRefreshToken(String refreshToken);

    record TokenClaims(String userId, String email, String accountType) {
    }

    record RefreshResult(String accessToken, String userId) {
    }
}
