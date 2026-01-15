package com.upkeep.domain.exception;
public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super("FORBIDDEN", message, 403);
    }
}
