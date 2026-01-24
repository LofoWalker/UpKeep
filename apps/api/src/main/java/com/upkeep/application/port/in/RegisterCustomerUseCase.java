package com.upkeep.application.port.in;

import com.upkeep.domain.model.customer.AccountType;

public interface RegisterCustomerUseCase {
    RegisterResult execute(RegisterCommand command);

    record RegisterCommand(
            String email,
            String password,
            String confirmPassword,
            AccountType accountType
    ) {
    }

    record RegisterResult(
            String customerId,
            String email,
            AccountType accountType
    ) {
    }
}
