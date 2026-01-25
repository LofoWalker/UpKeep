package com.upkeep.application.usecase;

import com.upkeep.application.port.in.AuthenticateCustomerUseCase;
import com.upkeep.application.port.out.auth.PasswordHasher;
import com.upkeep.application.port.out.auth.TokenService;
import com.upkeep.application.port.out.customer.CustomerRepository;
import com.upkeep.domain.exception.InvalidCredentialsException;
import com.upkeep.domain.model.customer.Customer;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.customer.Password;
import com.upkeep.domain.model.customer.PasswordHash;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthenticateCustomerUseCaseImpl implements AuthenticateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public AuthenticateCustomerUseCaseImpl(CustomerRepository customerRepository,
                                           PasswordHasher passwordHasher,
                                           TokenService tokenService) {
        this.customerRepository = customerRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public AuthResult execute(AuthCommand command) {
        Email email = new Email(command.email());
        Password password = new Password(command.password());

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        PasswordHash storedHash = customer.getPasswordHash()
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.verify(password, storedHash)) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenService.generateAccessToken(customer);
        String refreshToken = tokenService.generateRefreshToken(customer);

        return new AuthResult(
                accessToken,
                refreshToken,
                new UserInfo(
                        customer.getId().value().toString(),
                        customer.getEmail().value(),
                        customer.getAccountType()
                )
        );
    }
}
