package com.upkeep.infrastructure.adapter.in.rest.exception;

import com.upkeep.infrastructure.adapter.in.rest.response.ApiError;
import com.upkeep.infrastructure.adapter.in.rest.response.ApiResponse;
import jakarta.annotation.Priority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Provider
@Priority(1)
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = Logger.getLogger(ConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String traceId = UUID.randomUUID().toString();
        LOG.warnf("Constraint Violation [%s]: %s", traceId, exception.getMessage());

        List<ApiError.FieldError> fieldErrors = mapViolationsToFieldErrors(exception.getConstraintViolations());

        return Response
            .status(400)
            .entity(ApiResponse.error(
                ApiError.validation("Validation failed", fieldErrors, traceId)
            ))
            .build();
    }

    private List<ApiError.FieldError> mapViolationsToFieldErrors(Set<ConstraintViolation<?>> violations) {
        return violations.stream()
            .map(v -> {
                String path = v.getPropertyPath().toString();
                String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                return new ApiError.FieldError(field, v.getMessage());
            })
            .toList();
    }
}
