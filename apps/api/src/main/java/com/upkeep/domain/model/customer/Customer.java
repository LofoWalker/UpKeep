package com.upkeep.domain.model.customer;

import java.time.Instant;

public class Customer {
    private final CustomerId id;
    private final Email email;
    private final PasswordHash passwordHash;
    private final AccountType accountType;
    private final Instant createdAt;
    private Instant updatedAt;

    private Customer(CustomerId id,
                     Email email,
                     PasswordHash passwordHash,
                     AccountType accountType,
                     Instant createdAt,
                     Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountType = accountType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Customer create(Email email, PasswordHash passwordHash, AccountType accountType) {
        Instant now = Instant.now();
        return new Customer(
            CustomerId.generate(),
            email,
            passwordHash,
            accountType,
            now,
            now
        );
    }

    public static Customer reconstitute(CustomerId id,
                                         Email email,
                                         PasswordHash passwordHash,
                                         AccountType accountType,
                                         Instant createdAt,
                                         Instant updatedAt) {
        return new Customer(id, email, passwordHash, accountType, createdAt, updatedAt);
    }

    public void updateTimestamp() {
        this.updatedAt = Instant.now();
    }

    public CustomerId getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
