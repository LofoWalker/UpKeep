package com.upkeep.application.usecase;

import com.upkeep.application.port.in.RegisterCustomerUseCase;
import com.upkeep.application.port.out.EmailService;
import com.upkeep.application.port.out.PasswordHasher;
import com.upkeep.application.port.out.CustomerRepository;
import com.upkeep.domain.exception.ConflictException;
import com.upkeep.domain.exception.ValidationException;
import com.upkeep.domain.model.customer.*;
import com.upkeep.infrastructure.adapter.in.rest.response.ApiError;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class RegisterCustomerUseCaseImpl implements RegisterCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final PasswordHasher passwordHasher;
    private final EmailService emailService;

    public RegisterCustomerUseCaseImpl(CustomerRepository customerRepository, PasswordHasher passwordHasher, EmailService emailService) {
        this.customerRepository = customerRepository;
        this.passwordHasher = passwordHasher;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public RegisterResult execute(RegisterCommand command) {
        if (!command.password().equals(command.confirmPassword())) {
            throw new ValidationException("Passwords do not match", List.of(
                new ApiError.FieldError("confirmPassword", "Passwords do not match")
            ));
        }

        Email email = new Email(command.email());

        if (customerRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
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
