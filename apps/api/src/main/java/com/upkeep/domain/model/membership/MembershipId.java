package com.upkeep.domain.model.membership;

import java.util.UUID;

public record MembershipId(UUID value) {
    public static MembershipId generate() {
        return new MembershipId(UUID.randomUUID());
    }

    public static MembershipId from(String value) {
        return new MembershipId(UUID.fromString(value));
    }

    public static MembershipId from(UUID value) {
        return new MembershipId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
