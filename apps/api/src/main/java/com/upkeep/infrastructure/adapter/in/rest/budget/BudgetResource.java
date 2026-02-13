package com.upkeep.infrastructure.adapter.in.rest.budget;

import com.upkeep.application.port.in.budget.GetBudgetSummaryUseCase;
import com.upkeep.application.port.in.budget.GetBudgetSummaryUseCase.BudgetSummary;
import com.upkeep.application.port.in.budget.SetCompanyBudgetUseCase;
import com.upkeep.application.port.in.budget.SetCompanyBudgetUseCase.SetBudgetCommand;
import com.upkeep.application.port.in.budget.SetCompanyBudgetUseCase.SetBudgetResult;
import com.upkeep.application.port.in.budget.UpdateCompanyBudgetUseCase;
import com.upkeep.application.port.in.budget.UpdateCompanyBudgetUseCase.UpdateBudgetCommand;
import com.upkeep.application.port.in.budget.UpdateCompanyBudgetUseCase.UpdateBudgetResult;
import com.upkeep.application.port.out.auth.TokenService;
import com.upkeep.application.port.out.auth.TokenService.TokenClaims;
import com.upkeep.domain.model.budget.Currency;
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

@Path("/api/companies/{companyId}/budget")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BudgetResource {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final SetCompanyBudgetUseCase setCompanyBudgetUseCase;
    private final UpdateCompanyBudgetUseCase updateCompanyBudgetUseCase;
    private final GetBudgetSummaryUseCase getBudgetSummaryUseCase;
    private final TokenService tokenService;

    public BudgetResource(SetCompanyBudgetUseCase setCompanyBudgetUseCase,
                          UpdateCompanyBudgetUseCase updateCompanyBudgetUseCase,
                          GetBudgetSummaryUseCase getBudgetSummaryUseCase,
                          TokenService tokenService) {
        this.setCompanyBudgetUseCase = setCompanyBudgetUseCase;
        this.updateCompanyBudgetUseCase = updateCompanyBudgetUseCase;
        this.getBudgetSummaryUseCase = getBudgetSummaryUseCase;
        this.tokenService = tokenService;
    }

    @GET
    public Response getBudget(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                              @PathParam("companyId") String companyId) {
        TokenClaims claims = validateToken(accessToken);
        if (claims == null) {
            return unauthorizedResponse();
        }
        BudgetSummary summary = getBudgetSummaryUseCase.execute(companyId);
        BudgetSummaryResponse response = new BudgetSummaryResponse(
                summary.budgetId(),
                summary.totalCents(),
                summary.allocatedCents(),
                summary.remainingCents(),
                summary.currency(),
                summary.exists()
        );
        return Response.ok(ApiResponse.success(response)).build();
    }

    @POST
    public Response setBudget(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                              @PathParam("companyId") String companyId,
                              @Valid SetBudgetRequest request) {
        TokenClaims claims = validateToken(accessToken);

        if (claims == null) {
            return unauthorizedResponse();
        }

        Currency currency = Currency.valueOf(request.currency());

        SetBudgetResult result = setCompanyBudgetUseCase.execute(
                new SetBudgetCommand(
                        companyId,
                        claims.userId(),
                        request.amountCents(),
                        currency
                )
        );

        BudgetResponse response = new BudgetResponse(
                result.budgetId(),
                result.amountCents(),
                result.currency()
        );

        return Response.status(201)
                .entity(ApiResponse.success(response))
                .build();
    }

    @PATCH
    public Response updateBudget(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                                 @PathParam("companyId") String companyId,
                                 @Valid UpdateBudgetRequest request) {
        TokenClaims claims = validateToken(accessToken);

        if (claims == null) {
            return unauthorizedResponse();
        }

        Currency currency = Currency.valueOf(request.currency());

        UpdateBudgetResult result = updateCompanyBudgetUseCase.execute(
                new UpdateBudgetCommand(
                        companyId,
                        claims.userId(),
                        request.amountCents(),
                        currency
                )
        );

        UpdateBudgetResponse response = new UpdateBudgetResponse(
                result.budgetId(),
                result.amountCents(),
                result.currency(),
                result.isLowerThanAllocations(),
                result.currentAllocationsCents()
        );

        return Response.ok(ApiResponse.success(response)).build();
    }

    private TokenClaims validateToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        return tokenService.validateAccessToken(accessToken);
    }

    private Response unauthorizedResponse() {
        return Response.status(401)
                .entity(ApiResponse.error(ApiError.of("UNAUTHORIZED", "Authentication required", null)))
                .build();
    }
}
