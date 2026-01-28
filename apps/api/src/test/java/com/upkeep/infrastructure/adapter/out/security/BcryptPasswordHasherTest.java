package com.upkeep.infrastructure.adapter.out.security;

import com.upkeep.domain.model.customer.Password;
import com.upkeep.domain.model.customer.PasswordHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BcryptPasswordHasher")
class BcryptPasswordHasherTest {

    private BcryptPasswordHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new BcryptPasswordHasher();
    }

    @Test
    @DisplayName("should hash password to different value")
    void shouldHashPasswordToDifferentValue() {
        Password password = new Password("SecurePass123");

        PasswordHash hash = hasher.hash(password);

        assertNotNull(hash);
        assertNotEquals(password.value(), hash.value());
    }

    @Test
    @DisplayName("should generate different hashes for same password")
    void shouldGenerateDifferentHashesForSamePassword() {
        Password password = new Password("SecurePass123");

        PasswordHash hash1 = hasher.hash(password);
        PasswordHash hash2 = hasher.hash(password);

        assertNotEquals(hash1.value(), hash2.value());
    }

    @Test
    @DisplayName("should verify correct password")
    void shouldVerifyCorrectPassword() {
        Password password = new Password("SecurePass123");
        PasswordHash hash = hasher.hash(password);

        assertTrue(hasher.verify(password, hash));
    }

    @Test
    @DisplayName("should reject incorrect password")
    void shouldRejectIncorrectPassword() {
        Password password = new Password("SecurePass123");
        Password wrongPassword = new Password("WrongPass123");
        PasswordHash hash = hasher.hash(password);

        assertFalse(hasher.verify(wrongPassword, hash));
    }
}
