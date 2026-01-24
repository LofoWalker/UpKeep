package com.upkeep.application.usecase;

import com.upkeep.application.port.in.AuthenticateCustomerUseCase;
import com.upkeep.application.port.out.CustomerRepository;
import com.upkeep.application.port.out.PasswordHasher;
import com.upkeep.application.port.out.TokenService;
import com.upkeep.domain.exception.InvalidCredentialsException;
import com.upkeep.domain.model.customer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticateCustomerUseCaseImplTest {

    private CustomerRepository customerRepository;
    private PasswordHasher passwordHasher;
    private TokenService tokenService;
    private AuthenticateCustomerUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        tokenService = mock(TokenService.class);
        useCase = new AuthenticateCustomerUseCaseImpl(customerRepository, passwordHasher, tokenService);
    }

    @Test
    void shouldAuthenticateCustomerSuccessfully() {
        String email = "test@example.com";
        String password = "Password123";
        String accessToken = "access-token-value";
        String refreshToken = "refresh-token-value";

        Customer customer = createTestCustomer(email);

        when(customerRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(customer));
        when(passwordHasher.verify(any(Password.class), any(PasswordHash.class))).thenReturn(true);
        when(tokenService.generateAccessToken(any(Customer.class))).thenReturn(accessToken);
        when(tokenService.generateRefreshToken(any(Customer.class))).thenReturn(refreshToken);

        AuthenticateCustomerUseCase.AuthCommand command = new AuthenticateCustomerUseCase.AuthCommand(email, password);
        AuthenticateCustomerUseCase.AuthResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshToken, result.refreshToken());
        assertEquals(email, result.user().email());
        assertEquals(AccountType.COMPANY, result.user().accountType());

        verify(tokenService).generateAccessToken(customer);
        verify(tokenService).generateRefreshToken(customer);
    }

    @Test
    void shouldThrowExceptionWhenEmailNotFound() {
        when(customerRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        AuthenticateCustomerUseCase.AuthCommand command = new AuthenticateCustomerUseCase.AuthCommand(
                "unknown@example.com",
                "Password123"
        );

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> useCase.execute(command));
        assertEquals("Invalid email or password", exception.getMessage());

        verify(tokenService, never()).generateAccessToken(any());
        verify(tokenService, never()).generateRefreshToken(any());
    }

    @Test
    void shouldThrowExceptionWhenPasswordInvalid() {
        String email = "test@example.com";
        Customer customer = createTestCustomer(email);

        when(customerRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(customer));
        when(passwordHasher.verify(any(Password.class), any(PasswordHash.class))).thenReturn(false);

        AuthenticateCustomerUseCase.AuthCommand command = new AuthenticateCustomerUseCase.AuthCommand(
                email,
                "WrongPassword123"
        );

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> useCase.execute(command));
        assertEquals("Invalid email or password", exception.getMessage());

        verify(tokenService, never()).generateAccessToken(any());
        verify(tokenService, never()).generateRefreshToken(any());
    }

    @Test
    void shouldNotRevealWhichCredentialIsInvalid() {
        when(customerRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        AuthenticateCustomerUseCase.AuthCommand command = new AuthenticateCustomerUseCase.AuthCommand(
                "unknown@example.com",
                "Password123"
        );

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> useCase.execute(command));

        // Security: same error message for wrong email and wrong password
        assertEquals("Invalid email or password", exception.getMessage());
    }

    private Customer createTestCustomer(String email) {
        return Customer.reconstitute(
                CustomerId.from(UUID.randomUUID()),
                new Email(email),
                new PasswordHash("$2a$12$hashedPassword"),
                AccountType.COMPANY,
                Instant.now(),
                Instant.now()
        );
    }
}
