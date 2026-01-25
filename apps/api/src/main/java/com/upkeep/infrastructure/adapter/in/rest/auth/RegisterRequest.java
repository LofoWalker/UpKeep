package com.upkeep.infrastructure.adapter.in.rest.auth;

import com.upkeep.domain.model.customer.AccountType;
import com.upkeep.domain.model.customer.EmailValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Pattern(regexp = EmailValidation.EMAIL_REGEX, message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword,

        @NotNull(message = "Account type is required")
        AccountType accountType
) {}
