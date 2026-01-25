package com.upkeep.infrastructure.adapter.in.rest.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String code,
        String message,
        List<FieldError> details,
        String traceId
) {
    public record FieldError(String field, String message) {}

    public static ApiError of(String code, String message, String traceId) {
        return new ApiError(code, message, null, traceId);
    }

    public static ApiError validation(String message,
                                      List<com.upkeep.domain.exception.FieldError> domainErrors,
                                      String traceId) {
        List<FieldError> details = domainErrors.stream()
                .map(e -> new FieldError(e.field(), e.message()))
                .toList();
        return new ApiError("VALIDATION_ERROR", message, details, traceId);
    }

    public static ApiError validationFromApiFieldErrors(String message,
                                                        List<FieldError> details,
                                                        String traceId) {
        List<com.upkeep.domain.exception.FieldError> domainFieldErrors = details.stream()
                .map(d -> new com.upkeep.domain.exception.FieldError(d.field(), d.message()))
                .toList();

        return validation(message, domainFieldErrors, traceId);
    }
}
