# Story 1.5: User Registration with Email

Status: ✅ completed

## Story

As a **visitor**,
I want to create an account with my email and password,
so that I can access Upkeep.

## Acceptance Criteria

1. **Given** I am on the registration page  
   **When** I enter a valid email, password (min 8 chars, 1 uppercase, 1 number), and confirm password  
   **And** I submit the form  
   **Then** my account is created  
   **And** I am redirected to the onboarding flow  
   **And** I receive a welcome email

2. **Given** I enter an email that already exists  
   **When** I submit the form  
   **Then** I see an error: "An account with this email already exists"

3. **Given** I enter mismatched passwords  
   **When** I submit the form  
   **Then** I see an error: "Passwords do not match"

4. **Given** I enter an invalid password format  
   **When** I submit the form  
   **Then** I see specific validation errors for password requirements

## Tasks / Subtasks

- [x] Task 1: Create Customer domain model (AC: #1)
  - [x] 1.1: Create `Customer` entity in domain layer
  - [x] 1.2: Create `CustomerId` value object
  - [x] 1.3: Create `Email` value object with validation
  - [x] 1.4: Create `Password` value object with hashing

- [x] Task 2: Create registration use case (AC: #1, #2)
  - [x] 2.1: Create `RegisterCustomerUseCase` port interface
  - [x] 2.2: Implement `RegisterCustomerUseCaseImpl`
  - [x] 2.3: Create `CustomerRepository` port interface
  - [x] 2.4: Create `PasswordHasher` port interface

- [x] Task 3: Create infrastructure adapters (AC: #1, #2)
  - [x] 3.1: Implement `CustomerJpaRepository` adapter
  - [x] 3.2: Implement `BcryptPasswordHasher` adapter
  - [x] 3.3: Create database migration for customers table
  - [x] 3.4: Create `RegistrationResource` REST endpoint

- [x] Task 4: Create frontend registration (AC: #1, #2, #3, #4)
  - [x] 4.1: Create registration page component
  - [x] 4.2: Create registration form with validation
  - [x] 4.3: Implement API client for registration
  - [x] 4.4: Add client-side password validation
  - [x] 4.5: Handle success/error states

- [x] Task 5: Email notification (AC: #1)
  - [x] 5.1: Create `EmailService` port interface
  - [x] 5.2: Create welcome email template
  - [x] 5.3: Implement email adapter (can be mock for MVP)

## Dev Notes

### Domain Model

**Customer Entity:**
```java
package com.upkeep.domain.model.customer;

import java.time.Instant;
import java.util.UUID;

public class Customer {
    private final CustomerId id;
    private final Email email;
    private final PasswordHash passwordHash;
    private final AccountType accountType;
    private final Instant createdAt;
    private Instant updatedAt;

    public static Customer create(Email email, PasswordHash passwordHash, AccountType accountType) {
        return new Customer(
            CustomerId.generate(),
            email,
            passwordHash,
            accountType,
            Instant.now(),
            Instant.now()
        );
    }
}

public record CustomerId(UUID value) {
    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }
    public static CustomerId from(String value) {
        return new CustomerId(UUID.fromString(value));
    }
}

public record Email(String value) {
    public Email {
        if (value == null || !value.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new ValidationException("Invalid email format");
        }
        value = value.toLowerCase().trim();
    }
}

public enum AccountType {
    COMPANY, MAINTAINER, BOTH
}
```

### Password Validation Rules

```java
public record Password(String value) {
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern NUMBER = Pattern.compile("[0-9]");

    public Password {
        List<String> errors = new ArrayList<>();
        if (value.length() < MIN_LENGTH) {
            errors.add("Password must be at least 8 characters");
        }
        if (!UPPERCASE.matcher(value).find()) {
            errors.add("Password must contain at least one uppercase letter");
        }
        if (!NUMBER.matcher(value).find()) {
            errors.add("Password must contain at least one number");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Invalid password", errors);
        }
    }
}
```

### Use Case

```java
package com.upkeep.application.port.in;

public interface RegisterCustomerUseCase {
    RegisterResult execute(RegisterCommand command);

    record RegisterCommand(
        String email,
        String password,
        String confirmPassword,
        AccountType accountType
    ) {}

    record RegisterResult(String customerId, String email, AccountType accountType) {}
}

// Implementation
package com.upkeep.application.usecase;

@ApplicationScoped
public class RegisterCustomerUseCaseImpl implements RegisterCustomerUseCase {
    
    private final CustomerRepository customerRepository;
    private final PasswordHasher passwordHasher;
    private final EmailService emailService;

    @Override
    @Transactional
    public RegisterResult execute(RegisterCommand command) {
        // Validate passwords match
        if (!command.password().equals(command.confirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        // Validate email uniqueness
        Email email = new Email(command.email());
        if (customerRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        // Create customer
        Password password = new Password(command.password());
        PasswordHash hash = passwordHasher.hash(password);
        Customer customer = Customer.create(email, hash, command.accountType());

        customerRepository.save(customer);
        emailService.sendWelcomeEmail(email);

        return new RegisterResult(customer.getId().value().toString(), email.value(), customer.getAccountType());
    }
}
```

### REST Endpoint

```java
package com.upkeep.infrastructure.adapter.in.rest;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final RegisterCustomerUseCase registerCustomerUseCase;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        RegisterResult result = registerCustomerUseCase.execute(
            new RegisterCommand(
                request.email(),
                request.password(),
                request.confirmPassword(),
                request.accountType()
            )
        );
        return Response.status(201)
            .entity(ApiResponse.success(result))
            .build();
    }
}

public record RegisterRequest(
    @NotBlank @jakarta.validation.constraints.Email String email,
    @NotBlank String password,
    @NotBlank String confirmPassword,
    AccountType accountType
) {}
```

### Database Schema

```sql
-- V1__create_customers_table.sql
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers__email ON customers(email);
```

### Frontend Registration Form

```tsx
// apps/web/src/features/auth/components/RegisterForm.tsx
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'

const registerSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[0-9]/, 'Password must contain at least one number'),
  confirmPassword: z.string(),
}).refine(data => data.password === data.confirmPassword, {
  message: "Passwords do not match",
  path: ["confirmPassword"],
})

export function RegisterForm() {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(registerSchema)
  })

  const onSubmit = async (data) => {
    // API call
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Input {...register('email')} error={errors.email?.message} />
      <Input type="password" {...register('password')} error={errors.password?.message} />
      <Input type="password" {...register('confirmPassword')} error={errors.confirmPassword?.message} />
      <Button type="submit">Create Account</Button>
    </form>
  )
}
```

### Security Notes (NFR7)

- Passwords MUST be hashed with bcrypt (cost factor 12)
- Never log raw passwords
- Never return password hash in API responses
- Use timing-safe comparison for password verification

### Dependencies on Previous Stories

- Story 1.1: Hexagonal architecture structure
- Story 1.2: PostgreSQL database available
- Story 1.4: API envelope pattern

### References

- [Source: architecture.md#Authentication-Security] - Auth patterns
- [Source: epics.md#Story-1.5] - Original acceptance criteria
- NFR7: Secure password storage

## Dev Agent Record

### Agent Model Used

_To be filled by dev agent_

### Completion Notes List

_To be filled during implementation_

### Change Log

_To be filled during implementation_

### File List

_To be filled after implementation_

