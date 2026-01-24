package com.upkeep.infrastructure.adapter.in.rest.auth;

import com.upkeep.domain.model.customer.AccountType;

public record LoginResponse(
        String customerId,
        String email,
        AccountType accountType
) {}
