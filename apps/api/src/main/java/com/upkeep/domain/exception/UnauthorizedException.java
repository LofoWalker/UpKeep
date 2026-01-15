package com.upkeep.domain.exception;
public class UnauthorizedException extends ApiException {
    public UnauthorizedException() {
        super("UNAUTHORIZED", "Authentication required", 401);
    }
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, 401);
    }
}
