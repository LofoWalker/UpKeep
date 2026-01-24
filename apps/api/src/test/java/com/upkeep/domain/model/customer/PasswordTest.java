package com.upkeep.domain.model.customer;

import com.upkeep.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Value Object")
class PasswordTest {

    @Test
    @DisplayName("should accept valid password meeting all requirements")
    void shouldAcceptValidPassword() {
        Password password = new Password("SecurePass1");

        assertEquals("SecurePass1", password.value());
    }

    @ParameterizedTest
    @DisplayName("should accept various valid password formats")
    @ValueSource(strings = {
        "Password1",
        "ALLCAPS1lowercase",
        "12345678A",
        "A12345678",
        "ValidP@ss1",
        "SuperSecure123!"
    })
    void shouldAcceptVariousValidFormats(String validPassword) {
        Password password = new Password(validPassword);

        assertNotNull(password.value());
    }

    @ParameterizedTest
    @DisplayName("should reject null and blank passwords")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void shouldRejectNullAndBlankPasswords(String invalidPassword) {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new Password(invalidPassword)
        );

        assertTrue(exception.getFieldErrors().stream()
            .anyMatch(e -> e.field().equals("password") && e.message().contains("required")));
    }

    @ParameterizedTest
    @DisplayName("should reject passwords shorter than 8 characters")
    @ValueSource(strings = {
        "Pass1",
        "Ab1",
        "Short1"
    })
    void shouldRejectShortPasswords(String shortPassword) {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new Password(shortPassword)
        );

        assertTrue(exception.getFieldErrors().stream()
            .anyMatch(e -> e.message().contains("at least 8 characters")));
    }

    @ParameterizedTest
    @DisplayName("should reject passwords without uppercase letter")
    @ValueSource(strings = {
        "lowercase1",
        "nouppercasehere9",
        "12345678a"
    })
    void shouldRejectPasswordsWithoutUppercase(String noUppercase) {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new Password(noUppercase)
        );

        assertTrue(exception.getFieldErrors().stream()
            .anyMatch(e -> e.message().contains("uppercase letter")));
    }

    @ParameterizedTest
    @DisplayName("should reject passwords without number")
    @ValueSource(strings = {
        "NoNumbersHere",
        "Abcdefghij",
        "ALLUPPERCASENONUM"
    })
    void shouldRejectPasswordsWithoutNumber(String noNumber) {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new Password(noNumber)
        );

        assertTrue(exception.getFieldErrors().stream()
            .anyMatch(e -> e.message().contains("at least one number")));
    }

    @Test
    @DisplayName("should collect all validation errors when multiple rules fail")
    void shouldCollectAllValidationErrors() {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new Password("short")
        );

        assertTrue(exception.getFieldErrors().size() >= 2,
            "Should have at least 2 errors: length and uppercase");
    }

    @Test
    @DisplayName("password at exactly minimum length should be valid")
    void passwordAtMinimumLengthShouldBeValid() {
        Password password = new Password("Exactly8");

        assertEquals("Exactly8", password.value());
    }
}
