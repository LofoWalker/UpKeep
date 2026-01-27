package com.upkeep.application.usecase;

import com.upkeep.application.port.in.OAuthLoginUseCase.OAuthCommand;
import com.upkeep.application.port.in.OAuthLoginUseCase.OAuthResult;
import com.upkeep.application.port.out.auth.TokenService;
import com.upkeep.application.port.out.customer.CustomerRepository;
import com.upkeep.application.port.out.oauth.UserOAuthProviderRepository;
import com.upkeep.domain.model.customer.AccountType;
import com.upkeep.domain.model.customer.Customer;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.oauth.OAuthProvider;
import com.upkeep.domain.model.oauth.UserOAuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuthLoginUseCaseImplTest {

    private CustomerRepository customerRepository;
    private UserOAuthProviderRepository oauthProviderRepository;
    private TokenService tokenService;
    private OAuthLoginUseCaseImpl useCase;

    private static final String ACCESS_TOKEN = "mock-access-token";
    private static final String REFRESH_TOKEN = "mock-refresh-token";
    private static final String PROVIDER_USER_ID = "github-user-12345";
    private static final String USER_EMAIL = "oauth@example.com";

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        oauthProviderRepository = mock(UserOAuthProviderRepository.class);
        tokenService = mock(TokenService.class);
        useCase = new OAuthLoginUseCaseImpl(customerRepository, oauthProviderRepository, tokenService);

        when(tokenService.generateAccessToken(any(Customer.class))).thenReturn(ACCESS_TOKEN);
        when(tokenService.generateRefreshToken(any(Customer.class))).thenReturn(REFRESH_TOKEN);
    }

    @Test
    void shouldLoginExistingUserWithLinkedOAuthProvider() {
        Customer existingCustomer = createTestCustomer();
        UserOAuthProvider existingLink = createTestOAuthLink(existingCustomer.getId());

        when(oauthProviderRepository.findByProviderAndProviderUserId(OAuthProvider.GITHUB, PROVIDER_USER_ID))
                .thenReturn(Optional.of(existingLink));
        when(customerRepository.findById(existingCustomer.getId()))
                .thenReturn(Optional.of(existingCustomer));

        OAuthCommand command = new OAuthCommand(
                OAuthProvider.GITHUB,
                PROVIDER_USER_ID,
                USER_EMAIL,
                AccountType.COMPANY
        );

        OAuthResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(ACCESS_TOKEN, result.accessToken());
        assertEquals(REFRESH_TOKEN, result.refreshToken());
        assertFalse(result.isNewUser());
        assertEquals(USER_EMAIL, result.email());

        verify(customerRepository, never()).save(any());
        verify(oauthProviderRepository, never()).save(any());
    }

    @Test
    void shouldLinkOAuthToExistingUserByEmail() {
        Customer existingCustomer = createTestCustomer();

        when(oauthProviderRepository.findByProviderAndProviderUserId(OAuthProvider.GITHUB, PROVIDER_USER_ID))
                .thenReturn(Optional.empty());
        when(customerRepository.findByEmail(any(Email.class)))
                .thenReturn(Optional.of(existingCustomer));

        OAuthCommand command = new OAuthCommand(
                OAuthProvider.GITHUB,
                PROVIDER_USER_ID,
                USER_EMAIL,
                AccountType.COMPANY
        );

        OAuthResult result = useCase.execute(command);

        assertNotNull(result);
        assertFalse(result.isNewUser());
        assertEquals(USER_EMAIL, result.email());

        verify(customerRepository, never()).save(any());

        ArgumentCaptor<UserOAuthProvider> linkCaptor = ArgumentCaptor.forClass(UserOAuthProvider.class);
        verify(oauthProviderRepository).save(linkCaptor.capture());
        assertEquals(existingCustomer.getId(), linkCaptor.getValue().getUserId());
        assertEquals(OAuthProvider.GITHUB, linkCaptor.getValue().getProvider());
        assertEquals(PROVIDER_USER_ID, linkCaptor.getValue().getProviderUserId());
    }

    @Test
    void shouldCreateNewUserAndLinkOAuth() {
        when(oauthProviderRepository.findByProviderAndProviderUserId(OAuthProvider.GITHUB, PROVIDER_USER_ID))
                .thenReturn(Optional.empty());
        when(customerRepository.findByEmail(any(Email.class)))
                .thenReturn(Optional.empty());

        OAuthCommand command = new OAuthCommand(
                OAuthProvider.GITHUB,
                PROVIDER_USER_ID,
                USER_EMAIL,
                AccountType.MAINTAINER
        );

        OAuthResult result = useCase.execute(command);

        assertNotNull(result);
        assertTrue(result.isNewUser());
        assertEquals(USER_EMAIL, result.email());
        assertEquals(AccountType.MAINTAINER, result.accountType());

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals(USER_EMAIL, customerCaptor.getValue().getEmail().value());
        assertEquals(AccountType.MAINTAINER, customerCaptor.getValue().getAccountType());

        verify(oauthProviderRepository).save(any(UserOAuthProvider.class));
    }

    @Test
    void shouldThrowWhenLinkedUserNotFound() {
        CustomerId orphanUserId = CustomerId.from(UUID.randomUUID());
        UserOAuthProvider orphanLink = createTestOAuthLink(orphanUserId);

        when(oauthProviderRepository.findByProviderAndProviderUserId(OAuthProvider.GITHUB, PROVIDER_USER_ID))
                .thenReturn(Optional.of(orphanLink));
        when(customerRepository.findById(orphanUserId))
                .thenReturn(Optional.empty());

        OAuthCommand command = new OAuthCommand(
                OAuthProvider.GITHUB,
                PROVIDER_USER_ID,
                USER_EMAIL,
                AccountType.COMPANY
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(command)
        );

        assertEquals("User not found for OAuth provider link", exception.getMessage());
    }

    @Test
    void shouldReturnCorrectTokensForNewUser() {
        when(oauthProviderRepository.findByProviderAndProviderUserId(any(), any()))
                .thenReturn(Optional.empty());
        when(customerRepository.findByEmail(any(Email.class)))
                .thenReturn(Optional.empty());

        OAuthCommand command = new OAuthCommand(
                OAuthProvider.GITHUB,
                PROVIDER_USER_ID,
                USER_EMAIL,
                AccountType.COMPANY
        );

        OAuthResult result = useCase.execute(command);

        assertEquals(ACCESS_TOKEN, result.accessToken());
        assertEquals(REFRESH_TOKEN, result.refreshToken());
        verify(tokenService).generateAccessToken(any(Customer.class));
        verify(tokenService).generateRefreshToken(any(Customer.class));
    }

    @Test
    void shouldPreserveAccountTypeForNewUser() {
        when(oauthProviderRepository.findByProviderAndProviderUserId(any(), any()))
                .thenReturn(Optional.empty());
        when(customerRepository.findByEmail(any(Email.class)))
                .thenReturn(Optional.empty());

        OAuthCommand command = new OAuthCommand(
                OAuthProvider.GOOGLE,
                "google-user-789",
                "google@example.com",
                AccountType.BOTH
        );

        OAuthResult result = useCase.execute(command);

        assertEquals(AccountType.BOTH, result.accountType());

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals(AccountType.BOTH, customerCaptor.getValue().getAccountType());
    }

    private Customer createTestCustomer() {
        return Customer.reconstitute(
                CustomerId.from(UUID.randomUUID()),
                new Email(USER_EMAIL),
                null,
                AccountType.COMPANY,
                Instant.now(),
                Instant.now()
        );
    }

    private UserOAuthProvider createTestOAuthLink(CustomerId userId) {
        return UserOAuthProvider.reconstitute(
                UUID.randomUUID(),
                userId,
                OAuthProvider.GITHUB,
                PROVIDER_USER_ID,
                USER_EMAIL,
                Instant.now()
        );
    }
}
