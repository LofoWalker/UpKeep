package com.upkeep.application.usecase;

import com.upkeep.application.port.in.OAuthLoginUseCase;
import com.upkeep.application.port.out.auth.TokenService;
import com.upkeep.application.port.out.customer.CustomerRepository;
import com.upkeep.application.port.out.oauth.UserOAuthProviderRepository;
import com.upkeep.domain.model.customer.Customer;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.oauth.UserOAuthProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class OAuthLoginUseCaseImpl implements OAuthLoginUseCase {

    private final CustomerRepository customerRepository;
    private final UserOAuthProviderRepository oauthProviderRepository;
    private final TokenService tokenService;

    public OAuthLoginUseCaseImpl(CustomerRepository customerRepository,
                                 UserOAuthProviderRepository oauthProviderRepository,
                                 TokenService tokenService) {
        this.customerRepository = customerRepository;
        this.oauthProviderRepository = oauthProviderRepository;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public OAuthResult execute(OAuthCommand command) {
        Optional<UserOAuthProvider> existingLink = oauthProviderRepository
                .findByProviderAndProviderUserId(command.provider(), command.providerUserId());

        if (existingLink.isPresent()) {
            Customer customer = customerRepository.findById(existingLink.get().getUserId())
                    .orElseThrow(() -> new IllegalStateException(
                            "User not found for OAuth provider link"));
            return createAuthResult(customer, false);
        }

        Email email = new Email(command.email());
        Optional<Customer> existingUser = customerRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            Customer customer = existingUser.get();
            UserOAuthProvider link = UserOAuthProvider.create(
                    customer.getId(),
                    command.provider(),
                    command.providerUserId(),
                    command.email()
            );
            oauthProviderRepository.save(link);
            return createAuthResult(customer, false);
        }

        Customer newCustomer = Customer.createFromOAuth(email, command.accountType());
        customerRepository.save(newCustomer);

        UserOAuthProvider link = UserOAuthProvider.create(
                newCustomer.getId(),
                command.provider(),
                command.providerUserId(),
                command.email()
        );
        oauthProviderRepository.save(link);

        return createAuthResult(newCustomer, true);
    }

    private OAuthResult createAuthResult(Customer customer, boolean isNewUser) {
        String accessToken = tokenService.generateAccessToken(customer);
        String refreshToken = tokenService.generateRefreshToken(customer);
        return new OAuthResult(
                accessToken,
                refreshToken,
                isNewUser,
                customer.getId().value().toString(),
                customer.getEmail().value(),
                customer.getAccountType()
        );
    }
}
