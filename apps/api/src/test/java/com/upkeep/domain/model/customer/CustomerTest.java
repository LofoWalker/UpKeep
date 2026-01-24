package com.upkeep.domain.model.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Customer Entity")
class CustomerTest {

    @Test
    @DisplayName("create() should generate a new Customer with generated ID and timestamps")
    void create_shouldGenerateCustomerWithIdAndTimestamps() {
        Email email = new Email("user@example.com");
        PasswordHash passwordHash = new PasswordHash("$2a$10$hashedpassword");
        AccountType accountType = AccountType.COMPANY;

        Customer customer = Customer.create(email, passwordHash, accountType);

        assertNotNull(customer.getId());
        assertEquals(email, customer.getEmail());
        assertEquals(passwordHash, customer.getPasswordHash());
        assertEquals(accountType, customer.getAccountType());
        assertNotNull(customer.getCreatedAt());
        assertNotNull(customer.getUpdatedAt());
        assertEquals(customer.getCreatedAt(), customer.getUpdatedAt());
    }

    @Test
    @DisplayName("create() should assign COMPANY account type")
    void create_shouldAssignCompanyAccountType() {
        Customer customer = createCustomerWithAccountType(AccountType.COMPANY);

        assertEquals(AccountType.COMPANY, customer.getAccountType());
    }

    @Test
    @DisplayName("create() should assign MAINTAINER account type")
    void create_shouldAssignMaintainerAccountType() {
        Customer customer = createCustomerWithAccountType(AccountType.MAINTAINER);

        assertEquals(AccountType.MAINTAINER, customer.getAccountType());
    }

    @Test
    @DisplayName("create() should assign BOTH account type")
    void create_shouldAssignBothAccountType() {
        Customer customer = createCustomerWithAccountType(AccountType.BOTH);

        assertEquals(AccountType.BOTH, customer.getAccountType());
    }

    @Test
    @DisplayName("reconstitute() should restore Customer from persisted data")
    void reconstitute_shouldRestoreCustomerFromPersistedData() {
        CustomerId id = CustomerId.generate();
        Email email = new Email("persisted@example.com");
        PasswordHash passwordHash = new PasswordHash("$2a$10$hashedpassword");
        AccountType accountType = AccountType.MAINTAINER;
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2024-06-15T12:30:00Z");

        Customer customer = Customer.reconstitute(id, email, passwordHash, accountType, createdAt, updatedAt);

        assertEquals(id, customer.getId());
        assertEquals(email, customer.getEmail());
        assertEquals(passwordHash, customer.getPasswordHash());
        assertEquals(accountType, customer.getAccountType());
        assertEquals(createdAt, customer.getCreatedAt());
        assertEquals(updatedAt, customer.getUpdatedAt());
    }

    @Test
    @DisplayName("updateTimestamp() should update the updatedAt field")
    void updateTimestamp_shouldUpdateUpdatedAtField() throws InterruptedException {
        Customer customer = createDefaultCustomer();
        Instant originalUpdatedAt = customer.getUpdatedAt();

        Thread.sleep(10);
        customer.updateTimestamp();

        assertTrue(customer.getUpdatedAt().isAfter(originalUpdatedAt));
        assertNotNull(customer.getCreatedAt());
    }

    @Test
    @DisplayName("getId() should return immutable CustomerId")
    void getId_shouldReturnImmutableId() {
        Customer customer = createDefaultCustomer();
        CustomerId id1 = customer.getId();
        CustomerId id2 = customer.getId();

        assertEquals(id1, id2);
    }

    @Test
    @DisplayName("each create() call should generate unique ID")
    void create_shouldGenerateUniqueIds() {
        Customer customer1 = createDefaultCustomer();
        Customer customer2 = createDefaultCustomer();

        assertNotEquals(customer1.getId(), customer2.getId());
    }

    private Customer createDefaultCustomer() {
        return Customer.create(
            new Email("test@example.com"),
            new PasswordHash("$2a$10$hashedpassword"),
            AccountType.COMPANY
        );
    }

    private Customer createCustomerWithAccountType(AccountType accountType) {
        return Customer.create(
            new Email("test@example.com"),
            new PasswordHash("$2a$10$hashedpassword"),
            accountType
        );
    }
}
