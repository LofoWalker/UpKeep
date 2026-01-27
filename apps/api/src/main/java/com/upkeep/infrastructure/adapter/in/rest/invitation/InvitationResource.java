package com.upkeep.infrastructure.adapter.in.rest.invitation;

import com.upkeep.application.port.in.AcceptInvitationUseCase;
import com.upkeep.application.port.in.AcceptInvitationUseCase.AcceptInvitationCommand;
import com.upkeep.application.port.in.AcceptInvitationUseCase.AcceptInvitationResult;
import com.upkeep.application.port.in.GetInvitationUseCase;
import com.upkeep.application.port.in.GetInvitationUseCase.GetInvitationQuery;
import com.upkeep.application.port.in.GetInvitationUseCase.InvitationDetails;
import com.upkeep.application.port.out.auth.TokenService;
import com.upkeep.application.port.out.auth.TokenService.TokenClaims;
import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiError;
import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/invitations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InvitationResource {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final GetInvitationUseCase getInvitationUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final TokenService tokenService;

    public InvitationResource(GetInvitationUseCase getInvitationUseCase,
                              AcceptInvitationUseCase acceptInvitationUseCase,
                              TokenService tokenService) {
        this.getInvitationUseCase = getInvitationUseCase;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.tokenService = tokenService;
    }

    @GET
    @Path("/{token}")
    public Response getInvitation(@PathParam("token") String token) {
        InvitationDetails details = getInvitationUseCase.execute(new GetInvitationQuery(token));

        InvitationDetailsResponse response = new InvitationDetailsResponse(
                details.invitationId(),
                details.companyName(),
                details.role(),
                details.status(),
                details.isExpired(),
                details.expiresAt()
        );

        return Response.ok(ApiResponse.success(response)).build();
    }

    @POST
    @Path("/{token}/accept")
    public Response acceptInvitation(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                                     @PathParam("token") String token) {
        TokenClaims claims = validateToken(accessToken);
        if (claims == null) {
            return unauthorizedResponse();
        }

        AcceptInvitationResult result = acceptInvitationUseCase.execute(
                new AcceptInvitationCommand(claims.userId(), token)
        );

        AcceptInvitationResponse response = new AcceptInvitationResponse(
                result.companyId(),
                result.companyName(),
                result.companySlug(),
                result.membershipId(),
                result.role()
        );

        return Response.ok(ApiResponse.success(response)).build();
    }

    private TokenClaims validateToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        try {
            return tokenService.validateAccessToken(accessToken);
        } catch (Exception e) {
            return null;
        }
    }

    private Response unauthorizedResponse() {
        return Response.status(401)
                .entity(ApiResponse.error(new ApiError(
                        "UNAUTHORIZED", "Authentication required", null, null)))
                .build();
    }
}
