package com.upkeep.domain.exception;
import com.upkeep.infrastructure.adapter.in.rest.response.ApiError;
import java.util.List;
public class ValidationException extends ApiException {
    private final List<ApiError.FieldError> fieldErrors;
    public ValidationException(String message, List<ApiError.FieldError> fieldErrors) {
        super("VALIDATION_ERROR", message, 400);
        this.fieldErrors = fieldErrors;
    }
    public List<ApiError.FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
