package com.upkeep.domain.model.invitation;

import java.util.UUID;

public record InvitationId(UUID value) {
    public static InvitationId generate() {
        return new InvitationId(UUID.randomUUID());
    }

    public static InvitationId from(String value) {
        return new InvitationId(UUID.fromString(value));
    }

    public static InvitationId from(UUID value) {
        return new InvitationId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
