package com.upkeep.application.usecase;

import com.upkeep.application.port.in.RegisterCustomerUseCase;
import com.upkeep.application.port.out.auth.PasswordHasher;
import com.upkeep.application.port.out.customer.CustomerRepository;
import com.upkeep.application.port.out.notification.EmailService;
import com.upkeep.domain.exception.CustomerAlreadyExistsException;
import com.upkeep.domain.exception.DomainValidationException;
import com.upkeep.domain.exception.FieldError;
import com.upkeep.domain.model.customer.Customer;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.customer.Password;
import com.upkeep.domain.model.customer.PasswordHash;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class RegisterCustomerUseCaseImpl implements RegisterCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final PasswordHasher passwordHasher;
    private final EmailService emailService;

    public RegisterCustomerUseCaseImpl(CustomerRepository customerRepository,
                                       PasswordHasher passwordHasher,
                                       EmailService emailService) {
        this.customerRepository = customerRepository;
        this.passwordHasher = passwordHasher;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public RegisterResult execute(RegisterCommand command) {
        if (!command.password().equals(command.confirmPassword())) {
            throw new DomainValidationException("Passwords do not match", List.of(
                    new FieldError("confirmPassword", "Passwords do not match")
            ));
        }
        Email email = new Email(command.email());
        if (customerRepository.existsByEmail(email)) {
            throw new CustomerAlreadyExistsException(email.value());
        }
        Password password = new Password(command.password());
        PasswordHash hash = passwordHasher.hash(password);
        Customer customer = Customer.create(email, hash, command.accountType());
        customerRepository.save(customer);
        emailService.sendWelcomeEmail(email);
        return new RegisterResult(
                customer.getId().value().toString(),
                email.value(),
                customer.getAccountType()
        );
    }
}
