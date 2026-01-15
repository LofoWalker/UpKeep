package com.upkeep.domain.exception;
public class NotFoundException extends ApiException {
    public NotFoundException(String resource, String id) {
        super("NOT_FOUND", resource + " not found: " + id, 404);
    }
    public NotFoundException(String message) {
        super("NOT_FOUND", message, 404);
    }
}
