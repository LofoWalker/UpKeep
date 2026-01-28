package com.upkeep.domain.model.invitation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("InvitationId")
class InvitationIdTest {

    @Test
    @DisplayName("should generate non-null ID")
    void shouldGenerateNonNullId() {
        InvitationId id = InvitationId.generate();

        assertNotNull(id);
        assertNotNull(id.value());
    }

    @Test
    @DisplayName("should create ID from valid UUID string")
    void shouldCreateIdFromValidUuidString() {
        String uuidString = UUID.randomUUID().toString();

        InvitationId id = InvitationId.from(uuidString);

        assertNotNull(id);
        assertEquals(uuidString, id.value().toString());
    }

    @Test
    @DisplayName("should create ID from UUID")
    void shouldCreateIdFromUuid() {
        UUID uuid = UUID.randomUUID();

        InvitationId id = InvitationId.from(uuid);

        assertNotNull(id);
        assertEquals(uuid, id.value());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for invalid UUID string")
    void shouldThrowForInvalidUuidString() {
        assertThrows(IllegalArgumentException.class,
                () -> InvitationId.from("not-a-uuid"));
    }
}
