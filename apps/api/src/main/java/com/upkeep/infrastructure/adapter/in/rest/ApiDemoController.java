package com.upkeep.infrastructure.adapter.in.rest;
import com.upkeep.domain.exception.*;
import com.upkeep.infrastructure.adapter.in.rest.response.ApiError;
import com.upkeep.infrastructure.adapter.in.rest.response.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
@Path("/api/demo")
@Produces(MediaType.APPLICATION_JSON)
public class ApiDemoController {
    @GET
    @Path("/success")
    public ApiResponse<DemoData> success() {
        return ApiResponse.success(new DemoData("demo-123", "Success response"));
    }
    @GET
    @Path("/validation-error")
    public ApiResponse<DemoData> validationError() {
        throw new ValidationException(
            "Invalid input provided",
            List.of(
                new ApiError.FieldError("email", "Invalid email format"),
                new ApiError.FieldError("age", "Must be at least 18")
            )
        );
    }
    @GET
    @Path("/unauthorized")
    public ApiResponse<DemoData> unauthorized() {
        throw new UnauthorizedException();
    }
    @GET
    @Path("/forbidden")
    public ApiResponse<DemoData> forbidden() {
        throw new ForbiddenException("You don't have permission to access this resource");
    }
    @GET
    @Path("/not-found/{id}")
    public ApiResponse<DemoData> notFound(@PathParam("id") String id) {
        throw new NotFoundException("User", id);
    }
    @GET
    @Path("/conflict")
    public ApiResponse<DemoData> conflict() {
        throw new ConflictException("User with email already exists");
    }
    @GET
    @Path("/domain-rule")
    public ApiResponse<DemoData> domainRule() {
        throw new DomainRuleException("Cannot delete user with active subscriptions");
    }
    @GET
    @Path("/internal-error")
    public ApiResponse<DemoData> internalError() {
        throw new RuntimeException("Simulated unexpected error");
    }
    @GET
    @Path("/all-statuses")
    public ApiResponse<Map<String, String>> allStatuses() {
        return ApiResponse.success(Map.of(
            "200", "GET /api/demo/success",
            "400", "GET /api/demo/validation-error",
            "401", "GET /api/demo/unauthorized",
            "403", "GET /api/demo/forbidden",
            "404", "GET /api/demo/not-found/123",
            "409", "GET /api/demo/conflict",
            "422", "GET /api/demo/domain-rule",
            "500", "GET /api/demo/internal-error"
        ));
    }
    public record DemoData(String id, String message) {}
}
