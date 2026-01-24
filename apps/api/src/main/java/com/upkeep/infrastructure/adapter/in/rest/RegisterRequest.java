package com.upkeep.infrastructure.adapter.in.rest;

import com.upkeep.domain.model.customer.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(regexp = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    String password,

    @NotBlank(message = "Password confirmation is required")
    String confirmPassword,

    @NotNull(message = "Account type is required")
    AccountType accountType
) {}
