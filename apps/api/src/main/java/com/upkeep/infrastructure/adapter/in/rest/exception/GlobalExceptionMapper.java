package com.upkeep.infrastructure.adapter.in.rest.exception;

import com.upkeep.domain.exception.ApiException;
import com.upkeep.domain.exception.ValidationException;
import com.upkeep.infrastructure.adapter.in.rest.response.ApiError;
import com.upkeep.infrastructure.adapter.in.rest.response.ApiResponse;
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

        if (exception instanceof ValidationException validationEx) {
            LOG.warnf("Validation Exception [%s]: %s", traceId, validationEx.getMessage());
            return Response
                .status(validationEx.getHttpStatus())
                .entity(ApiResponse.error(
                    ApiError.validation(
                        validationEx.getMessage(),
                        validationEx.getFieldErrors(),
                        traceId
                    )
                ))
                .build();
        }

        if (exception instanceof ApiException apiEx) {
            LOG.warnf("API Exception [%s]: %s", traceId, apiEx.getMessage());
            return Response
                .status(apiEx.getHttpStatus())
                .entity(ApiResponse.error(
                    ApiError.of(apiEx.getCode(), apiEx.getMessage(), traceId)
                ))
                .build();
        }

        LOG.errorf(exception, "Unexpected error [%s]", traceId);
        return Response
            .status(500)
            .entity(ApiResponse.error(
                ApiError.of("INTERNAL_ERROR", "An unexpected error occurred", traceId)
            ))
            .build();
    }
}
