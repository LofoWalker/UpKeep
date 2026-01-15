package com.upkeep.infrastructure.adapter.in.rest.response;
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
    public static ApiError validation(String message, List<FieldError> details, String traceId) {
        return new ApiError("VALIDATION_ERROR", message, details, traceId);
    }
}
