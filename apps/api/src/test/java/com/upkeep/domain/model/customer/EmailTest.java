package com.upkeep.domain.model.customer;

import com.upkeep.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email Value Object")
class EmailTest {

    @Test
    @DisplayName("should accept valid email address")
    void shouldAcceptValidEmail() {
        Email email = new Email("user@example.com");

        assertEquals("user@example.com", email.value());
    }

    @ParameterizedTest
    @DisplayName("should accept various valid email formats")
    @ValueSource(strings = {
            "user@example.com",
            "user.name@example.com",
            "user-name@example.com",
            "user_name@example.com",
            "user123@example.co.uk",
            "user@sub.domain.com"
    })
    void shouldAcceptVariousValidFormats(String validEmail) {
        Email email = new Email(validEmail);

        assertNotNull(email.value());
    }

    @Test
    @DisplayName("should normalize email to lowercase")
    void shouldNormalizeToLowercase() {
        Email email = new Email("User@EXAMPLE.COM");

        assertEquals("user@example.com", email.value());
    }

    @Test
    @DisplayName("should trim whitespace from email")
    void shouldTrimWhitespace() {
        Email email = new Email("  user@example.com  ");

        assertEquals("user@example.com", email.value());
    }

    @ParameterizedTest
    @DisplayName("should reject null and blank emails")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void shouldRejectNullAndBlankEmails(String invalidEmail) {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Email(invalidEmail)
        );

        assertEquals("Email is required", exception.getMessage());
        assertFalse(exception.getFieldErrors().isEmpty());
        assertEquals("email", exception.getFieldErrors().getFirst().field());
    }

    @ParameterizedTest
    @DisplayName("should reject invalid email formats")
    @ValueSource(strings = {
            "invalid",
            "invalid@",
            "@example.com",
            "user@.com",
            "user@example",
            "user example@test.com",
            "user@@example.com"
    })
    void shouldRejectInvalidFormats(String invalidEmail) {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Email(invalidEmail)
        );

        assertEquals("Invalid email format", exception.getMessage());
        assertEquals("email", exception.getFieldErrors().getFirst().field());
    }

    @Test
    @DisplayName("equals() should return true for same normalized value")
    void equals_shouldReturnTrueForSameNormalizedValue() {
        Email email1 = new Email("user@example.com");
        Email email2 = new Email("USER@EXAMPLE.COM");

        assertEquals(email1, email2);
    }
}
