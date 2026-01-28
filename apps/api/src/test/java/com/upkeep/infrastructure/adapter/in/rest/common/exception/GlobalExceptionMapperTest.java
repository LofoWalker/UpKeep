package com.upkeep.infrastructure.adapter.in.rest.common.exception;

import com.upkeep.domain.exception.AlreadyMemberException;
import com.upkeep.domain.exception.CompanyNotFoundException;
import com.upkeep.domain.exception.CompanySlugAlreadyExistsException;
import com.upkeep.domain.exception.CustomerAlreadyExistsException;
import com.upkeep.domain.exception.CustomerNotFoundException;
import com.upkeep.domain.exception.DomainException;
import com.upkeep.domain.exception.DomainValidationException;
import com.upkeep.domain.exception.FieldError;
import com.upkeep.domain.exception.InvalidCredentialsException;
import com.upkeep.domain.exception.InvalidRefreshTokenException;
import com.upkeep.domain.exception.InvitationAlreadyExistsException;
import com.upkeep.domain.exception.InvitationExpiredException;
import com.upkeep.domain.exception.InvitationNotFoundException;
import com.upkeep.domain.exception.LastOwnerException;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.exception.UnauthorizedOperationException;
import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("GlobalExceptionMapper")
class GlobalExceptionMapperTest {

    private GlobalExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GlobalExceptionMapper();
    }

    @Nested
    @DisplayName("Validation Exceptions")
    class ValidationExceptions {

        @Test
        @DisplayName("should return 400 with field errors for DomainValidationException")
        void shouldReturn400ForDomainValidationException() {
            List<FieldError> fieldErrors = List.of(
                new FieldError("email", "Email is required")
            );
            DomainValidationException exception = new DomainValidationException("Validation failed", fieldErrors);

            Response response = mapper.toResponse(exception);

            assertEquals(400, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("VALIDATION_ERROR", body.error().code());
            assertNotNull(body.error().traceId());
            assertNotNull(body.error().details());
        }
    }

    @Nested
    @DisplayName("Authentication Exceptions")
    class AuthenticationExceptions {

        @Test
        @DisplayName("should return 401 with INVALID_CREDENTIALS for InvalidCredentialsException")
        void shouldReturn401ForInvalidCredentialsException() {
            InvalidCredentialsException exception = new InvalidCredentialsException();

            Response response = mapper.toResponse(exception);

            assertEquals(401, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("INVALID_CREDENTIALS", body.error().code());
            assertNotNull(body.error().traceId());
        }

        @Test
        @DisplayName("should return 401 with INVALID_TOKEN for InvalidRefreshTokenException")
        void shouldReturn401ForInvalidRefreshTokenException() {
            InvalidRefreshTokenException exception = InvalidRefreshTokenException.notFound();

            Response response = mapper.toResponse(exception);

            assertEquals(401, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("INVALID_TOKEN", body.error().code());
            assertNotNull(body.error().traceId());
        }
    }

    @Nested
    @DisplayName("Not Found Exceptions")
    class NotFoundExceptions {

        @Test
        @DisplayName("should return 404 with CUSTOMER_NOT_FOUND for CustomerNotFoundException")
        void shouldReturn404ForCustomerNotFoundException() {
            CustomerNotFoundException exception = new CustomerNotFoundException("test@example.com");

            Response response = mapper.toResponse(exception);

            assertEquals(404, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("CUSTOMER_NOT_FOUND", body.error().code());
            assertNotNull(body.error().traceId());
        }

        @Test
        @DisplayName("should return 404 with COMPANY_NOT_FOUND for CompanyNotFoundException")
        void shouldReturn404ForCompanyNotFoundException() {
            CompanyNotFoundException exception = new CompanyNotFoundException("company-id");

            Response response = mapper.toResponse(exception);

            assertEquals(404, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("COMPANY_NOT_FOUND", body.error().code());
            assertNotNull(body.error().traceId());
        }

        @Test
        @DisplayName("should return 404 with NOT_A_MEMBER for MembershipNotFoundException")
        void shouldReturn404ForMembershipNotFoundException() {
            MembershipNotFoundException exception = new MembershipNotFoundException("user-id", "company-id");

            Response response = mapper.toResponse(exception);

            assertEquals(404, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("NOT_A_MEMBER", body.error().code());
            assertNotNull(body.error().traceId());
        }

        @Test
        @DisplayName("should return 404 with INVITATION_NOT_FOUND for InvitationNotFoundException")
        void shouldReturn404ForInvitationNotFoundException() {
            InvitationNotFoundException exception = new InvitationNotFoundException("token");

            Response response = mapper.toResponse(exception);

            assertEquals(404, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("INVITATION_NOT_FOUND", body.error().code());
            assertNotNull(body.error().traceId());
        }
    }

    @Nested
    @DisplayName("Conflict Exceptions")
    class ConflictExceptions {

        @Test
        @DisplayName("should return 409 with CUSTOMER_ALREADY_EXISTS for CustomerAlreadyExistsException")
        void shouldReturn409ForCustomerAlreadyExistsException() {
            CustomerAlreadyExistsException exception = new CustomerAlreadyExistsException("test@example.com");

            Response response = mapper.toResponse(exception);

            assertEquals(409, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("CUSTOMER_ALREADY_EXISTS", body.error().code());
            assertNotNull(body.error().traceId());
        }

        @Test
        @DisplayName("should return 409 with COMPANY_SLUG_ALREADY_EXISTS for CompanySlugAlreadyExistsException")
        void shouldReturn409ForCompanySlugAlreadyExistsException() {
            CompanySlugAlreadyExistsException exception = new CompanySlugAlreadyExistsException("test-slug");

            Response response = mapper.toResponse(exception);

            assertEquals(409, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("COMPANY_SLUG_ALREADY_EXISTS", body.error().code());
            assertNotNull(body.error().traceId());
        }

        @Test
        @DisplayName("should return 409 with INVITATION_ALREADY_EXISTS for InvitationAlreadyExistsException")
        void shouldReturn409ForInvitationAlreadyExistsException() {
            InvitationAlreadyExistsException exception = new InvitationAlreadyExistsException("test@example.com");

            Response response = mapper.toResponse(exception);

            assertEquals(409, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("INVITATION_ALREADY_EXISTS", body.error().code());
            assertNotNull(body.error().traceId());
        }

        @Test
        @DisplayName("should return 409 with ALREADY_MEMBER for AlreadyMemberException")
        void shouldReturn409ForAlreadyMemberException() {
            AlreadyMemberException exception = new AlreadyMemberException();

            Response response = mapper.toResponse(exception);

            assertEquals(409, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("ALREADY_MEMBER", body.error().code());
            assertNotNull(body.error().traceId());
        }
    }

    @Nested
    @DisplayName("Business Rule Exceptions")
    class BusinessRuleExceptions {

        @Test
        @DisplayName("should return 410 with INVITATION_EXPIRED for InvitationExpiredException")
        void shouldReturn410ForInvitationExpiredException() {
            InvitationExpiredException exception = new InvitationExpiredException();

            Response response = mapper.toResponse(exception);

            assertEquals(410, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("INVITATION_EXPIRED", body.error().code());
            assertNotNull(body.error().traceId());
        }

        @Test
        @DisplayName("should return 403 with FORBIDDEN for UnauthorizedOperationException")
        void shouldReturn403ForUnauthorizedOperationException() {
            UnauthorizedOperationException exception = new UnauthorizedOperationException("Unauthorized operation");

            Response response = mapper.toResponse(exception);

            assertEquals(403, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("FORBIDDEN", body.error().code());
            assertNotNull(body.error().traceId());
        }

        @Test
        @DisplayName("should return 422 with LAST_OWNER for LastOwnerException")
        void shouldReturn422ForLastOwnerException() {
            LastOwnerException exception = new LastOwnerException();

            Response response = mapper.toResponse(exception);

            assertEquals(422, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("LAST_OWNER", body.error().code());
            assertNotNull(body.error().traceId());
        }

        @Test
        @DisplayName("should return 422 with DOMAIN_ERROR for generic DomainException")
        void shouldReturn422ForGenericDomainException() {
            DomainException exception = new DomainException("Generic domain error") {};

            Response response = mapper.toResponse(exception);

            assertEquals(422, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("DOMAIN_ERROR", body.error().code());
            assertNotNull(body.error().traceId());
        }
    }

    @Nested
    @DisplayName("Unexpected Exceptions")
    class UnexpectedExceptions {

        @Test
        @DisplayName("should return 500 with INTERNAL_ERROR for unexpected RuntimeException")
        void shouldReturn500ForUnexpectedException() {
            RuntimeException exception = new RuntimeException("Unexpected error");

            Response response = mapper.toResponse(exception);

            assertEquals(500, response.getStatus());
            ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
            assertNotNull(body.error());
            assertEquals("INTERNAL_ERROR", body.error().code());
            assertNotNull(body.error().traceId());
        }
    }
}
