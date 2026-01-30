package com.upkeep.domain.model.audit;

import java.util.UUID;

public record AuditEventId(UUID value) {
    public static AuditEventId generate() {
        return new AuditEventId(UUID.randomUUID());
    }

    public static AuditEventId from(String value) {
        return new AuditEventId(UUID.fromString(value));
    }

    public static AuditEventId from(UUID value) {
        return new AuditEventId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
