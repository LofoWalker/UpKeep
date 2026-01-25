package com.upkeep.application.port.in;

import com.upkeep.domain.model.customer.AccountType;

public interface AuthenticateCustomerUseCase {
    AuthResult execute(AuthCommand command);

    record AuthCommand(String email, String password) {
    }

    record AuthResult(String accessToken, String refreshToken, UserInfo user) {
    }

    record UserInfo(String id, String email, AccountType accountType) {
    }
}
