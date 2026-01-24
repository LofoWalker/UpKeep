package com.upkeep.infrastructure.adapter.in.rest.health;

import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    public ApiResponse<HealthData> getHealth() {
        return ApiResponse.success(new HealthData("UP", "Upkeep API is running"));
    }

    public record HealthData(String status, String message) {}
}
