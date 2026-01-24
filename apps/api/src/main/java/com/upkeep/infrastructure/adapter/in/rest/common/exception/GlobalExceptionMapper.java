package com.upkeep.infrastructure.adapter.in.rest.common.exception;

import com.upkeep.domain.exception.CustomerAlreadyExistsException;
import com.upkeep.domain.exception.CustomerNotFoundException;
import com.upkeep.domain.exception.DomainException;
import com.upkeep.domain.exception.DomainValidationException;
import com.upkeep.domain.exception.InvalidCredentialsException;
import com.upkeep.domain.exception.InvalidRefreshTokenException;
import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiError;
import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiResponse;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.UUID;

@Provider
@Priority(Priorities.USER)
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        String traceId = UUID.randomUUID().toString();

        if (exception instanceof DomainException domainEx) {
            return handleDomainException(domainEx, traceId);
        }

        LOG.errorf(exception, "Unexpected error [%s]", traceId);
        return Response
                .status(500)
                .entity(ApiResponse.error(
                        ApiError.of("INTERNAL_ERROR", "An unexpected error occurred", traceId)
                ))
                .build();
    }

    private Response handleDomainException(DomainException exception, String traceId) {
        LOG.warnf("Domain Exception [%s]: %s", traceId, exception.getMessage());

        return switch (exception) {
            case DomainValidationException e -> Response
                    .status(400)
                    .entity(ApiResponse.error(
                            ApiError.validation(e.getMessage(), e.getFieldErrors(), traceId)
                    ))
                    .build();

            case InvalidCredentialsException e -> Response
                    .status(401)
                    .entity(ApiResponse.error(
                            ApiError.of("INVALID_CREDENTIALS", e.getMessage(), traceId)
                    ))
                    .build();

            case InvalidRefreshTokenException e -> Response
                    .status(401)
                    .entity(ApiResponse.error(
                            ApiError.of("INVALID_TOKEN", e.getMessage(), traceId)
                    ))
                    .build();

            case CustomerNotFoundException e -> Response
                    .status(404)
                    .entity(ApiResponse.error(
                            ApiError.of("CUSTOMER_NOT_FOUND", e.getMessage(), traceId)
                    ))
                    .build();

            case CustomerAlreadyExistsException e -> Response
                    .status(409)
                    .entity(ApiResponse.error(
                            ApiError.of("CUSTOMER_ALREADY_EXISTS", e.getMessage(), traceId)
                    ))
                    .build();

            default -> Response
                    .status(422)
                    .entity(ApiResponse.error(
                            ApiError.of("DOMAIN_ERROR", exception.getMessage(), traceId)
                    ))
                    .build();
        };
    }
}
