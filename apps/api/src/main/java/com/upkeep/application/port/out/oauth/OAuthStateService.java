package com.upkeep.application.port.out.oauth;

import com.upkeep.domain.model.customer.AccountType;

import java.util.Optional;

public interface OAuthStateService {

    String generateState(AccountType accountType);

    Optional<StateData> consumeState(String state);

    record StateData(
            AccountType accountType
    ) {
    }
}
