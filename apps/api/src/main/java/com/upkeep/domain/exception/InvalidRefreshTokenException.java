package com.upkeep.domain.exception;

/**
 * Thrown when a refresh token is invalid, expired, or revoked.
 */
public class InvalidRefreshTokenException extends DomainException {

    public InvalidRefreshTokenException(String reason) {
        super("Invalid refresh token: " + reason);
    }

    public static InvalidRefreshTokenException notFound() {
        return new InvalidRefreshTokenException("token not found");
    }

    public static InvalidRefreshTokenException expired() {
        return new InvalidRefreshTokenException("token expired");
    }

    public static InvalidRefreshTokenException revoked() {
        return new InvalidRefreshTokenException("token revoked");
    }
}
