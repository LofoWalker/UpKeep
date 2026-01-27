package com.upkeep.domain.exception;

public class LastOwnerException extends DomainException {
    public LastOwnerException() {
        super("Cannot remove the last Owner from the company");
    }
}
