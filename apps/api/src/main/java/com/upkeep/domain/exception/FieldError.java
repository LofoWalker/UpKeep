package com.upkeep.domain.exception;

/**
 * Represents a validation error for a specific field in the domain layer.
 */
public record FieldError(String field, String message) {
}
