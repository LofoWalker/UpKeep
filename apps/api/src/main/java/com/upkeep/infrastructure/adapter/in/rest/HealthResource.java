package com.upkeep.infrastructure.adapter.in.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    @Path("/ping")
    public PingResponse ping() {
        return new PingResponse("pong", System.currentTimeMillis());
    }

    public record PingResponse(String message, long timestamp) {}
}

