package com.upkeep.domain.exception;

public class UnauthorizedOperationException extends DomainException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
