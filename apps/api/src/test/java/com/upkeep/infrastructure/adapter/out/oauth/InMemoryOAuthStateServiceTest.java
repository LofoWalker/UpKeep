package com.upkeep.infrastructure.adapter.out.oauth;

import com.upkeep.application.port.out.oauth.OAuthStateService;
import com.upkeep.domain.model.customer.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InMemoryOAuthStateService")
class InMemoryOAuthStateServiceTest {

    private InMemoryOAuthStateService service;

    @BeforeEach
    void setUp() {
        service = new InMemoryOAuthStateService();
    }

    @Test
    @DisplayName("should generate state with 43 characters for COMPANY account type")
    void shouldGenerateStateWithCorrectLength() {
        String state = service.generateState(AccountType.COMPANY);

        assertNotNull(state);
        assertEquals(43, state.length());
    }

    @Test
    @DisplayName("should consume valid state and return account type")
    void shouldConsumeValidState() {
        String state = service.generateState(AccountType.COMPANY);

        Optional<OAuthStateService.StateData> result = service.consumeState(state);

        assertTrue(result.isPresent());
        assertEquals(AccountType.COMPANY, result.get().accountType());
    }

    @Test
    @DisplayName("should remove state after consumption")
    void shouldRemoveStateAfterConsumption() {
        String state = service.generateState(AccountType.COMPANY);

        service.consumeState(state);
        Optional<OAuthStateService.StateData> secondAttempt = service.consumeState(state);

        assertTrue(secondAttempt.isEmpty());
    }

    @Test
    @DisplayName("should return empty for non-existent state")
    void shouldReturnEmptyForNonExistentState() {
        Optional<OAuthStateService.StateData> result = service.consumeState("non-existent-state");


        assertTrue(result.isEmpty());
    }
}
