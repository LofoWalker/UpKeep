package com.upkeep.infrastructure.adapter.in.rest;

import com.upkeep.application.port.in.RegisterCustomerUseCase;
import com.upkeep.infrastructure.adapter.in.rest.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final RegisterCustomerUseCase registerCustomerUseCase;

    public AuthResource(RegisterCustomerUseCase registerCustomerUseCase) {
        this.registerCustomerUseCase = registerCustomerUseCase;
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        RegisterCustomerUseCase.RegisterResult result = registerCustomerUseCase.execute(
            new RegisterCustomerUseCase.RegisterCommand(
                request.email(),
                request.password(),
                request.confirmPassword(),
                request.accountType()
            )
        );
        return Response.status(201)
            .entity(ApiResponse.success(result))
            .build();
    }
}

