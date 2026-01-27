package com.upkeep.application.usecase;

import com.upkeep.application.port.in.RegisterCustomerUseCase;
import com.upkeep.application.port.out.auth.PasswordHasher;
import com.upkeep.application.port.out.customer.CustomerRepository;
import com.upkeep.application.port.out.notification.EmailService;
import com.upkeep.domain.exception.CustomerAlreadyExistsException;
import com.upkeep.domain.exception.DomainValidationException;
import com.upkeep.domain.model.customer.AccountType;
import com.upkeep.domain.model.customer.Customer;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.customer.Password;
import com.upkeep.domain.model.customer.PasswordHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterCustomerUseCaseImplTest {

    private CustomerRepository customerRepository;
    private PasswordHasher passwordHasher;
    private EmailService emailService;
    private RegisterCustomerUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        emailService = mock(EmailService.class);
        useCase = new RegisterCustomerUseCaseImpl(customerRepository, passwordHasher, emailService);
    }

    @Test
    void shouldRegisterCustomerSuccessfully() {
        String email = "test@example.com";
        String password = "Password123";
        AccountType accountType = AccountType.COMPANY;

        when(customerRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordHasher.hash(any(Password.class)))
                .thenReturn(new PasswordHash("$2a$12$hashedPassword"));

        RegisterCustomerUseCase.RegisterCommand command = new RegisterCustomerUseCase.RegisterCommand(
                email,
                password,
                password,
                accountType
        );

        RegisterCustomerUseCase.RegisterResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(email, result.email());
        assertEquals(accountType, result.accountType());
        assertNotNull(result.customerId());

        verify(customerRepository).save(any(Customer.class));
        verify(emailService).sendWelcomeEmail(any(Email.class));
    }

    @Test
    void shouldThrowExceptionWhenPasswordsDoNotMatch() {
        RegisterCustomerUseCase.RegisterCommand command = new RegisterCustomerUseCase.RegisterCommand(
                "test@example.com",
                "Password123",
                "DifferentPassword123",
                AccountType.COMPANY
        );

        assertThrows(DomainValidationException.class, () -> useCase.execute(command));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        String email = "existing@example.com";
        when(customerRepository.existsByEmail(any(Email.class))).thenReturn(true);

        RegisterCustomerUseCase.RegisterCommand command = new RegisterCustomerUseCase.RegisterCommand(
                email,
                "Password123",
                "Password123",
                AccountType.COMPANY
        );

        assertThrows(CustomerAlreadyExistsException.class, () -> useCase.execute(command));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionForInvalidEmail() {
        RegisterCustomerUseCase.RegisterCommand command = new RegisterCustomerUseCase.RegisterCommand(
                "invalid-email",
                "Password123",
                "Password123",
                AccountType.COMPANY
        );

        assertThrows(DomainValidationException.class, () -> useCase.execute(command));
    }

    @Test
    void shouldThrowExceptionForWeakPassword() {
        RegisterCustomerUseCase.RegisterCommand command = new RegisterCustomerUseCase.RegisterCommand(
                "test@example.com",
                "weak",
                "weak",
                AccountType.COMPANY
        );

        assertThrows(DomainValidationException.class, () -> useCase.execute(command));
    }
}
