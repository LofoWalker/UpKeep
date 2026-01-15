package com.upkeep.infrastructure.adapter.in.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Health check REST endpoint.
 * Simple driving adapter to verify API is running.
 */
@Path("/health")
public class HealthController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public HealthResponse getHealth() {
        return new HealthResponse("UP", "Upkeep API is running");
    }

    public static class HealthResponse {
        public String status;
        public String message;

        public HealthResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}

