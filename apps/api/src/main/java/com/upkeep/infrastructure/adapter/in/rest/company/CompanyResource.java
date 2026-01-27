package com.upkeep.infrastructure.adapter.in.rest.company;

import com.upkeep.application.port.in.CreateCompanyUseCase;
import com.upkeep.application.port.in.CreateCompanyUseCase.CreateCompanyCommand;
import com.upkeep.application.port.in.CreateCompanyUseCase.CreateCompanyResult;
import com.upkeep.application.port.in.GetCompanyDashboardUseCase;
import com.upkeep.application.port.in.GetCompanyDashboardUseCase.CompanyDashboard;
import com.upkeep.application.port.in.GetCompanyDashboardUseCase.GetCompanyDashboardQuery;
import com.upkeep.application.port.in.GetCompanyMembersUseCase;
import com.upkeep.application.port.in.GetCompanyMembersUseCase.GetCompanyMembersQuery;
import com.upkeep.application.port.in.GetCompanyMembersUseCase.MemberInfo;
import com.upkeep.application.port.in.GetUserCompaniesUseCase;
import com.upkeep.application.port.in.GetUserCompaniesUseCase.CompanyWithMembership;
import com.upkeep.application.port.in.GetUserCompaniesUseCase.GetUserCompaniesQuery;
import com.upkeep.application.port.in.InviteUserToCompanyUseCase;
import com.upkeep.application.port.in.InviteUserToCompanyUseCase.InviteCommand;
import com.upkeep.application.port.in.InviteUserToCompanyUseCase.InviteResult;
import com.upkeep.application.port.in.UpdateMemberRoleUseCase;
import com.upkeep.application.port.in.UpdateMemberRoleUseCase.UpdateMemberRoleCommand;
import com.upkeep.application.port.in.UpdateMemberRoleUseCase.UpdateMemberRoleResult;
import com.upkeep.application.port.out.auth.TokenService;
import com.upkeep.application.port.out.auth.TokenService.TokenClaims;
import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiError;
import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/companies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyResource {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final CreateCompanyUseCase createCompanyUseCase;
    private final GetUserCompaniesUseCase getUserCompaniesUseCase;
    private final GetCompanyDashboardUseCase getCompanyDashboardUseCase;
    private final InviteUserToCompanyUseCase inviteUserToCompanyUseCase;
    private final GetCompanyMembersUseCase getCompanyMembersUseCase;
    private final UpdateMemberRoleUseCase updateMemberRoleUseCase;
    private final TokenService tokenService;

    public CompanyResource(CreateCompanyUseCase createCompanyUseCase,
                           GetUserCompaniesUseCase getUserCompaniesUseCase,
                           GetCompanyDashboardUseCase getCompanyDashboardUseCase,
                           InviteUserToCompanyUseCase inviteUserToCompanyUseCase,
                           GetCompanyMembersUseCase getCompanyMembersUseCase,
                           UpdateMemberRoleUseCase updateMemberRoleUseCase,
                           TokenService tokenService) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.getUserCompaniesUseCase = getUserCompaniesUseCase;
        this.getCompanyDashboardUseCase = getCompanyDashboardUseCase;
        this.inviteUserToCompanyUseCase = inviteUserToCompanyUseCase;
        this.getCompanyMembersUseCase = getCompanyMembersUseCase;
        this.updateMemberRoleUseCase = updateMemberRoleUseCase;
        this.tokenService = tokenService;
    }

    @POST
    public Response createCompany(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                                  @Valid CreateCompanyRequest request) {
        TokenClaims claims = validateToken(accessToken);
        if (claims == null) {
            return unauthorizedResponse();
        }

        CreateCompanyResult result = createCompanyUseCase.execute(
                new CreateCompanyCommand(
                        claims.userId(),
                        request.name(),
                        request.slug()
                )
        );

        CompanyResponse response = new CompanyResponse(
                result.companyId(),
                result.name(),
                result.slug(),
                new CompanyResponse.MembershipResponse(
                        result.membership().membershipId(),
                        result.membership().role()
                )
        );

        return Response.status(201)
                .entity(ApiResponse.success(response))
                .build();
    }

    @GET
    public Response getUserCompanies(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken) {
        TokenClaims claims = validateToken(accessToken);
        if (claims == null) {
            return unauthorizedResponse();
        }

        List<CompanyWithMembership> companies = getUserCompaniesUseCase.execute(
                new GetUserCompaniesQuery(claims.userId())
        );

        List<CompanyListResponse> response = companies.stream()
                .map(c -> new CompanyListResponse(c.companyId(), c.name(), c.slug(), c.role()))
                .toList();

        return Response.ok(ApiResponse.success(response)).build();
    }

    @GET
    @Path("/{companyId}/dashboard")
    public Response getCompanyDashboard(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                                        @PathParam("companyId") String companyId) {
        TokenClaims claims = validateToken(accessToken);
        if (claims == null) {
            return unauthorizedResponse();
        }

        CompanyDashboard dashboard = getCompanyDashboardUseCase.execute(
                new GetCompanyDashboardQuery(claims.userId(), companyId)
        );

        CompanyDashboardResponse response = new CompanyDashboardResponse(
                dashboard.companyId(),
                dashboard.name(),
                dashboard.slug(),
                dashboard.userRole(),
                new CompanyDashboardResponse.StatsResponse(
                        dashboard.stats().totalMembers(),
                        dashboard.stats().hasBudget(),
                        dashboard.stats().hasPackages(),
                        dashboard.stats().hasAllocations()
                )
        );

        return Response.ok(ApiResponse.success(response)).build();
    }

    @POST
    @Path("/{companyId}/invitations")
    public Response inviteUser(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                               @PathParam("companyId") String companyId,
                               @Valid InviteUserRequest request) {
        TokenClaims claims = validateToken(accessToken);
        if (claims == null) {
            return unauthorizedResponse();
        }

        InviteResult result = inviteUserToCompanyUseCase.execute(
                new InviteCommand(
                        claims.userId(),
                        companyId,
                        request.email(),
                        request.role()
                )
        );

        InvitationResponse response = new InvitationResponse(
                result.invitationId(),
                result.email(),
                result.role(),
                result.status(),
                result.expiresAt()
        );

        return Response.status(201)
                .entity(ApiResponse.success(response))
                .build();
    }

    @GET
    @Path("/{companyId}/members")
    public Response getCompanyMembers(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                                      @PathParam("companyId") String companyId) {
        TokenClaims claims = validateToken(accessToken);
        if (claims == null) {
            return unauthorizedResponse();
        }

        List<MemberInfo> members = getCompanyMembersUseCase.execute(
                new GetCompanyMembersQuery(claims.userId(), companyId)
        );

        List<MemberResponse> response = members.stream()
                .map(m -> new MemberResponse(
                        m.membershipId(),
                        m.customerId(),
                        m.email(),
                        m.role(),
                        m.joinedAt()
                ))
                .toList();

        return Response.ok(ApiResponse.success(response)).build();
    }

    @PATCH
    @Path("/{companyId}/members/{membershipId}")
    public Response updateMemberRole(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                                     @PathParam("companyId") String companyId,
                                     @PathParam("membershipId") String membershipId,
                                     @Valid UpdateMemberRoleRequest request) {
        TokenClaims claims = validateToken(accessToken);
        if (claims == null) {
            return unauthorizedResponse();
        }

        UpdateMemberRoleResult result = updateMemberRoleUseCase.execute(
                new UpdateMemberRoleCommand(
                        claims.userId(),
                        companyId,
                        membershipId,
                        request.role()
                )
        );

        return Response.ok(ApiResponse.success(result)).build();
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
