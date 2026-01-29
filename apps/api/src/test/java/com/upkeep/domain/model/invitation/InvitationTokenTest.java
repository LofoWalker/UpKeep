package com.upkeep.domain.model.invitation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("InvitationToken")
class InvitationTokenTest {

    @Test
    @DisplayName("should generate token with 43 characters")
    void shouldGenerateTokenWithCorrectLength() {
        InvitationToken token = InvitationToken.generate();

        assertNotNull(token);
        assertEquals(43, token.value().length());
    }

    @Test
    @DisplayName("should create token from valid value")
    void shouldCreateTokenFromValidValue() {
        String validValue = "some-valid-token-value";

        InvitationToken token = InvitationToken.from(validValue);

        assertNotNull(token);
        assertEquals(validValue, token.value());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when value is null")
    void shouldThrowWhenValueIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new InvitationToken(null));
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when value is empty")
    void shouldThrowWhenValueIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new InvitationToken(""));
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when value is blank")
    void shouldThrowWhenValueIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new InvitationToken("   "));
    }
}
