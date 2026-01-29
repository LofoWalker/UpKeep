package com.upkeep.infrastructure.adapter.in.rest.common.exception;

import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ConstraintViolationExceptionMapper")
class ConstraintViolationExceptionMapperTest {

    private ConstraintViolationExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ConstraintViolationExceptionMapper();
    }

    @Test
    @DisplayName("should return 400 with single field error")
    void shouldReturn400WithSingleFieldError() {
        ConstraintViolation<?> violation = createMockViolation("email", "Email is required");
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        Response response = mapper.toResponse(exception);

        assertEquals(400, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertNotNull(body.error());
        assertEquals("VALIDATION_ERROR", body.error().code());
        assertNotNull(body.error().traceId());
        assertNotNull(body.error().details());
        assertEquals(1, body.error().details().size());
    }

    @Test
    @DisplayName("should return 400 with multiple field errors")
    void shouldReturn400WithMultipleFieldErrors() {
        ConstraintViolation<?> violation1 = createMockViolation("email", "Email is required");
        ConstraintViolation<?> violation2 = createMockViolation("password", "Password is required");
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation1, violation2));

        Response response = mapper.toResponse(exception);

        assertEquals(400, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertNotNull(body.error());
        assertEquals("VALIDATION_ERROR", body.error().code());
        assertNotNull(body.error().details());
        assertEquals(2, body.error().details().size());
    }

    @Test
    @DisplayName("should extract field name from nested path")
    void shouldExtractFieldNameFromNestedPath() {
        ConstraintViolation<?> violation = createMockViolation("request.email", "Email is required");
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        Response response = mapper.toResponse(exception);

        assertEquals(400, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertNotNull(body.error());
        List<?> details = body.error().details();
        assertNotNull(details);
        assertEquals(1, details.size());
    }

    private ConstraintViolation<?> createMockViolation(String propertyPath, String message) {
        @SuppressWarnings("unchecked")
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn(propertyPath);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);

        return violation;
    }
}
