package com.upkeep.domain.exception;

public class InvitationNotFoundException extends DomainException {
    private final String token;

    public InvitationNotFoundException(String token) {
        super("Invitation not found");
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
