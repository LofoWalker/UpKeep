# Story 1.4: API Envelope & Error Handling Pattern

Status: ready-for-dev

## Story

As a **developer**,
I want a standardized API response envelope,
so that all endpoints return consistent JSON structures.

## Acceptance Criteria

1. **Given** any API endpoint  
   **When** the request succeeds  
   **Then** the response body follows: `{ "data": <payload>, "meta": { ... } }`  
   **And** HTTP status is 200 or 201

2. **Given** any API endpoint  
   **When** the request fails due to validation  
   **Then** the response body follows: `{ "error": { "code": "VALIDATION_ERROR", "message": "...", "details": [...] } }`  
   **And** HTTP status is 400

3. **Given** any API endpoint  
   **When** the request fails due to authentication  
   **Then** HTTP status is 401 with error envelope

4. **Given** any API endpoint  
   **When** the request fails due to authorization  
   **Then** HTTP status is 403 with error envelope

5. **Given** any exception is thrown  
   **When** the API processes the request  
   **Then** a global exception handler maps exceptions to proper error envelopes

6. **Given** the codebase  
   **When** I look for response utilities  
   **Then** an `ApiResponse<T>` wrapper class exists in the infrastructure layer

## Tasks / Subtasks

- [ ] Task 1: Create API response models (AC: #1, #6)
  - [ ] 1.1: Create `ApiResponse<T>` generic wrapper
  - [ ] 1.2: Create `ApiMeta` class for metadata
  - [ ] 1.3: Create `ApiError` class for error details
  - [ ] 1.4: Create response builder utilities

- [ ] Task 2: Create exception hierarchy (AC: #2, #3, #4)
  - [ ] 2.1: Create base `ApiException` class
  - [ ] 2.2: Create `ValidationException` (400)
  - [ ] 2.3: Create `UnauthorizedException` (401)
  - [ ] 2.4: Create `ForbiddenException` (403)
  - [ ] 2.5: Create `NotFoundException` (404)
  - [ ] 2.6: Create `ConflictException` (409)
  - [ ] 2.7: Create `DomainRuleException` (422)

- [ ] Task 3: Implement global exception handler (AC: #5)
  - [ ] 3.1: Create `GlobalExceptionMapper` class
  - [ ] 3.2: Map each exception type to HTTP status
  - [ ] 3.3: Add traceId to all error responses
  - [ ] 3.4: Log errors with appropriate levels

- [ ] Task 4: Create tests and sample endpoint (AC: #1-#5)
  - [ ] 4.1: Create test endpoint demonstrating all cases
  - [ ] 4.2: Write integration tests for each HTTP status
  - [ ] 4.3: Document API conventions in README

## Dev Notes

### API Response Structure

```json
// Success response
{
  "data": { ... },
  "meta": {
    "timestamp": "2026-01-10T12:00:00.000Z",
    "traceId": "abc-123-def",
    "page": 1,
    "pageSize": 20,
    "totalItems": 100
  }
}

// Error response
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input provided",
    "details": [
      { "field": "email", "message": "Invalid email format" }
    ],
    "traceId": "abc-123-def"
  }
}
```

### Java Implementation

**ApiResponse.java:**
```java
package com.upkeep.infrastructure.adapter.in.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    T data,
    ApiMeta meta,
    ApiError error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, ApiMeta.now(), null);
    }

    public static <T> ApiResponse<T> success(T data, ApiMeta meta) {
        return new ApiResponse<>(data, meta, null);
    }

    public static <T> ApiResponse<T> error(ApiError error) {
        return new ApiResponse<>(null, null, error);
    }
}
```

**ApiMeta.java:**
```java
package com.upkeep.infrastructure.adapter.in.rest.response;

import java.time.Instant;
import java.util.UUID;

public record ApiMeta(
    Instant timestamp,
    String traceId,
    Integer page,
    Integer pageSize,
    Long totalItems
) {
    public static ApiMeta now() {
        return new ApiMeta(
            Instant.now(),
            UUID.randomUUID().toString(),
            null, null, null
        );
    }

    public static ApiMeta paged(int page, int pageSize, long totalItems) {
        return new ApiMeta(
            Instant.now(),
            UUID.randomUUID().toString(),
            page, pageSize, totalItems
        );
    }
}
```

**ApiError.java:**
```java
package com.upkeep.infrastructure.adapter.in.rest.response;

import java.util.List;

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
```

### Exception Hierarchy

```java
// Base exception
package com.upkeep.domain.exception;

public abstract class ApiException extends RuntimeException {
    private final String code;
    private final int httpStatus;

    protected ApiException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}

// Specific exceptions
public class ValidationException extends ApiException {
    private final List<FieldError> fieldErrors;
    
    public ValidationException(String message, List<FieldError> fieldErrors) {
        super("VALIDATION_ERROR", message, 400);
        this.fieldErrors = fieldErrors;
    }
}

public class UnauthorizedException extends ApiException {
    public UnauthorizedException() {
        super("UNAUTHORIZED", "Authentication required", 401);
    }
}

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super("FORBIDDEN", message, 403);
    }
}

public class NotFoundException extends ApiException {
    public NotFoundException(String resource, String id) {
        super("NOT_FOUND", resource + " not found: " + id, 404);
    }
}

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super("CONFLICT", message, 409);
    }
}

public class DomainRuleException extends ApiException {
    public DomainRuleException(String message) {
        super("DOMAIN_RULE_VIOLATION", message, 422);
    }
}
```

### Global Exception Mapper

```java
package com.upkeep.infrastructure.adapter.in.rest.exception;

import com.upkeep.domain.exception.ApiException;
import com.upkeep.infrastructure.adapter.in.rest.response.ApiError;
import com.upkeep.infrastructure.adapter.in.rest.response.ApiResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.util.UUID;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    
    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        String traceId = UUID.randomUUID().toString();

        if (exception instanceof ApiException apiEx) {
            LOG.warnf("API Exception [%s]: %s", traceId, apiEx.getMessage());
            return Response
                .status(apiEx.getHttpStatus())
                .entity(ApiResponse.error(
                    ApiError.of(apiEx.getCode(), apiEx.getMessage(), traceId)
                ))
                .build();
        }

        // Unexpected exception - log full stack trace
        LOG.errorf(exception, "Unexpected error [%s]", traceId);
        return Response
            .status(500)
            .entity(ApiResponse.error(
                ApiError.of("INTERNAL_ERROR", "An unexpected error occurred", traceId)
            ))
            .build();
    }
}
```

### HTTP Status Code Reference

| Status | Code | When to Use |
|--------|------|-------------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST creating resource |
| 400 | Bad Request | Validation errors |
| 401 | Unauthorized | Missing or invalid auth |
| 403 | Forbidden | Valid auth but insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource state conflict (e.g., duplicate) |
| 422 | Unprocessable Entity | Domain rule violation |
| 500 | Internal Error | Unexpected server error |

### Package Structure

```
com.upkeep/
├── domain/
│   └── exception/
│       ├── ApiException.java
│       ├── ValidationException.java
│       ├── UnauthorizedException.java
│       ├── ForbiddenException.java
│       ├── NotFoundException.java
│       ├── ConflictException.java
│       └── DomainRuleException.java
└── infrastructure/
    └── adapter/
        └── in/
            └── rest/
                ├── exception/
                │   └── GlobalExceptionMapper.java
                └── response/
                    ├── ApiResponse.java
                    ├── ApiMeta.java
                    └── ApiError.java
```

### Dependencies on Previous Stories

- Story 1.1 must be complete (hexagonal architecture structure)

### References

- [Source: architecture.md#Format-Patterns] - API envelope specification
- [Source: architecture.md#API-Communication-Patterns] - HTTP status conventions
- [Source: epics.md#Story-1.4] - Original acceptance criteria

## Dev Agent Record

### Agent Model Used

_To be filled by dev agent_

### Completion Notes List

_To be filled during implementation_

### Change Log

_To be filled during implementation_

### File List

_To be filled after implementation_

