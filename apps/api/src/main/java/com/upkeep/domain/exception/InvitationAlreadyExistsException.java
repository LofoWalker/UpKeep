package com.upkeep.domain.exception;

public class InvitationAlreadyExistsException extends DomainException {
    private final String email;

    public InvitationAlreadyExistsException(String email) {
        super("An invitation is already pending for this email: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
