package com.upkeep.domain.exception;

public class InvitationExpiredException extends DomainException {
    public InvitationExpiredException() {
        super("This invitation has expired");
    }
}
