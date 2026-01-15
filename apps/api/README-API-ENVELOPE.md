# API Envelope & Error Handling
This document describes the standardized API response format and error handling patterns implemented in the Upkeep API.
## Response Format
### Success Response
All successful API responses follow this structure:
```json
{
  "data": { ... },
  "meta": {
    "timestamp": "2026-01-15T12:00:00.000Z",
    "traceId": "abc-123-def",
    "page": 1,
    "pageSize": 20,
    "totalItems": 100
  }
}
```
- `data`: The actual response payload
- `meta`: Metadata about the response
  - `timestamp`: ISO 8601 timestamp of the response
  - `traceId`: Unique identifier for request tracing
  - `page`, `pageSize`, `totalItems`: Optional pagination metadata
### Error Response
All error responses follow this structure:
```json
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
- `error`: Error information
  - `code`: Machine-readable error code
  - `message`: Human-readable error message
  - `details`: Optional array of field-specific errors (for validation errors)
  - `traceId`: Unique identifier for error tracing
## HTTP Status Codes
| Status | Code | When to Use |
|--------|------|-------------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST creating resource |
| 400 | Bad Request | Validation errors |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Valid auth but insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource state conflict (e.g., duplicate) |
| 422 | Unprocessable Entity | Domain rule violation |
| 500 | Internal Error | Unexpected server error |
## Exception Types
### ValidationException (400)
Used when request input fails validation.
```java
throw new ValidationException(
    "Invalid input provided",
    List.of(
        new ApiError.FieldError("email", "Invalid email format"),
        new ApiError.FieldError("age", "Must be at least 18")
    )
);
```
### UnauthorizedException (401)
Used when authentication is required but not provided.
```java
throw new UnauthorizedException();
// or
throw new UnauthorizedException("Invalid token");
```
### ForbiddenException (403)
Used when user is authenticated but lacks permissions.
```java
throw new ForbiddenException("You don't have permission to access this resource");
```
### NotFoundException (404)
Used when a requested resource doesn't exist.
```java
throw new NotFoundException("User", userId);
// or
throw new NotFoundException("Resource not found");
```
### ConflictException (409)
Used when there's a resource state conflict.
```java
throw new ConflictException("User with email already exists");
```
### DomainRuleException (422)
Used when a domain business rule is violated.
```java
throw new DomainRuleException("Cannot delete user with active subscriptions");
```
## Demo Endpoints
The API includes demo endpoints to test all response formats:
- `GET /api/demo/success` - Returns 200 success response
- `GET /api/demo/validation-error` - Returns 400 validation error
- `GET /api/demo/unauthorized` - Returns 401 unauthorized
- `GET /api/demo/forbidden` - Returns 403 forbidden
- `GET /api/demo/not-found/{id}` - Returns 404 not found
- `GET /api/demo/conflict` - Returns 409 conflict
- `GET /api/demo/domain-rule` - Returns 422 domain rule violation
- `GET /api/demo/internal-error` - Returns 500 internal server error
- `GET /api/demo/all-statuses` - Returns list of all demo endpoints
## Usage in Controllers
### Returning Success Response
```java
@GET
@Path("/users/{id}")
public ApiResponse<User> getUser(@PathParam("id") String id) {
    User user = userService.findById(id);
    return ApiResponse.success(user);
}
```
### Returning Paginated Response
```java
@GET
@Path("/users")
public ApiResponse<List<User>> getUsers(
    @QueryParam("page") int page,
    @QueryParam("size") int size
) {
    List<User> users = userService.findAll(page, size);
    long total = userService.count();
    return ApiResponse.success(
        users,
        ApiMeta.paged(page, size, total)
    );
}
```
### Throwing Exceptions
Exceptions are automatically caught and converted to error responses by the `GlobalExceptionMapper`.
```java
@POST
@Path("/users")
public ApiResponse<User> createUser(CreateUserRequest request) {
    // Validation errors
    if (request.email() == null) {
        throw new ValidationException(
            "Invalid input",
            List.of(new ApiError.FieldError("email", "Email is required"))
        );
    }
    // Domain rule violations
    if (userService.emailExists(request.email())) {
        throw new ConflictException("Email already exists");
    }
    User user = userService.create(request);
    return ApiResponse.success(user);
}
```
## Implementation Details
### Package Structure
```
com.upkeep/
├── domain/
│   └── exception/
│       ├── ApiException.java              # Base exception class
│       ├── ValidationException.java       # 400 errors
│       ├── UnauthorizedException.java     # 401 errors
│       ├── ForbiddenException.java        # 403 errors
│       ├── NotFoundException.java         # 404 errors
│       ├── ConflictException.java         # 409 errors
│       └── DomainRuleException.java       # 422 errors
└── infrastructure/
    └── adapter/
        └── in/
            └── rest/
                ├── exception/
                │   └── GlobalExceptionMapper.java   # Exception handler
                └── response/
                    ├── ApiResponse.java             # Response wrapper
                    ├── ApiMeta.java                 # Response metadata
                    └── ApiError.java                # Error structure
```
### Global Exception Handler
The `GlobalExceptionMapper` automatically intercepts all exceptions and converts them to appropriate error responses. It:
1. Handles `ValidationException` specially to include field-level errors
2. Handles all `ApiException` subclasses with their specific HTTP status codes
3. Catches unexpected exceptions and returns 500 with a generic message
4. Adds a unique `traceId` to all error responses for debugging
5. Logs errors appropriately (warnings for API exceptions, errors for unexpected exceptions)
## Testing
Run the test suite to verify the implementation:
```bash
mvn test
```
Tests verify:
- Success responses include data and metadata
- Error responses include error code, message, and traceId
- Correct HTTP status codes are returned
- Validation errors include field-specific details
- Unexpected errors are handled gracefully
