package com.upkeep.domain.exception;

public class AlreadyMemberException extends DomainException {
    public AlreadyMemberException() {
        super("You are already a member of this company");
    }
}
