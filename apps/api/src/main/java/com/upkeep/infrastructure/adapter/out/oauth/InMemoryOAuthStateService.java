package com.upkeep.infrastructure.adapter.out.oauth;

import com.upkeep.application.port.out.oauth.OAuthStateService;
import com.upkeep.domain.model.customer.AccountType;
import jakarta.enterprise.context.ApplicationScoped;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryOAuthStateService implements OAuthStateService {

    private static final int STATE_EXPIRY_MINUTES = 10;
    private static final int STATE_LENGTH = 32;

    private final Map<String, StateEntry> stateStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateState(AccountType accountType) {
        cleanupExpiredStates();

        byte[] bytes = new byte[STATE_LENGTH];
        secureRandom.nextBytes(bytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant expiresAt = Instant.now().plusSeconds(STATE_EXPIRY_MINUTES * 60L);
        stateStore.put(state, new StateEntry(accountType, expiresAt));

        return state;
    }

    @Override
    public Optional<StateData> consumeState(String state) {
        StateEntry entry = stateStore.remove(state);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(new StateData(entry.accountType()));
    }

    private void cleanupExpiredStates() {
        Instant now = Instant.now();
        stateStore.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }

    private record StateEntry(AccountType accountType, Instant expiresAt) {}
}
