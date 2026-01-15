package com.upkeep.domain.exception;
public abstract class ApiException extends RuntimeException {
    private final String code;
    private final int httpStatus;
    protected ApiException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
    protected ApiException(String code, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }
    public String getCode() {
        return code;
    }
    public int getHttpStatus() {
        return httpStatus;
    }
}
