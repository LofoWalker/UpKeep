package com.upkeep.infrastructure.adapter.in.rest.auth;

public record MeResponse(
        String customerId,
        String email,
        String accountType
) {}
