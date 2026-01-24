package com.upkeep.domain.model.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerId Value Object")
class CustomerIdTest {

    @Test
    @DisplayName("generate() should create a unique CustomerId")
    void generate_shouldCreateUniqueId() {
        CustomerId id1 = CustomerId.generate();
        CustomerId id2 = CustomerId.generate();

        assertNotNull(id1.value());
        assertNotNull(id2.value());
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("from() should parse valid UUID string")
    void from_shouldParseValidUuidString() {
        String uuidString = "550e8400-e29b-41d4-a716-446655440000";

        CustomerId id = CustomerId.from(uuidString);

        assertNotNull(id);
        assertEquals(UUID.fromString(uuidString), id.value());
    }

    @Test
    @DisplayName("from() should throw exception for invalid UUID string")
    void from_shouldThrowForInvalidUuidString() {
        assertThrows(IllegalArgumentException.class, () -> CustomerId.from("invalid-uuid"));
    }

    @Test
    @DisplayName("from() should throw exception for null value")
    void from_shouldThrowForNullValue() {
        assertThrows(NullPointerException.class, () -> CustomerId.from((String) null));
    }

    @Test
    @DisplayName("toString() should return UUID string representation")
    void toString_shouldReturnUuidString() {
        String uuidString = "550e8400-e29b-41d4-a716-446655440000";
        CustomerId id = CustomerId.from(uuidString);

        assertEquals(uuidString, id.toString());
    }

    @Test
    @DisplayName("equals() should return true for same UUID value")
    void equals_shouldReturnTrueForSameValue() {
        String uuidString = "550e8400-e29b-41d4-a716-446655440000";
        CustomerId id1 = CustomerId.from(uuidString);
        CustomerId id2 = CustomerId.from(uuidString);

        assertEquals(id1, id2);
    }
}
