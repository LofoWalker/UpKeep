package com.upkeep.domain.model.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PasswordHash Value Object")
class PasswordHashTest {

    @Test
    @DisplayName("should accept valid password hash")
    void shouldAcceptValidHash() {
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        PasswordHash passwordHash = new PasswordHash(hash);

        assertEquals(hash, passwordHash.value());
    }

    @ParameterizedTest
    @DisplayName("should reject null and blank values")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void shouldRejectNullAndBlankValues(String invalidHash) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PasswordHash(invalidHash)
        );
    }

    @Test
    @DisplayName("should throw IllegalArgumentException with descriptive message for null")
    void shouldThrowWithDescriptiveMessageForNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PasswordHash(null)
        );

        assertEquals("Password hash cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException with descriptive message for empty")
    void shouldThrowWithDescriptiveMessageForEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PasswordHash("")
        );

        assertEquals("Password hash cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("equals() should return true for same hash value")
    void equals_shouldReturnTrueForSameValue() {
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        PasswordHash hash1 = new PasswordHash(hash);
        PasswordHash hash2 = new PasswordHash(hash);

        assertEquals(hash1, hash2);
    }
}
