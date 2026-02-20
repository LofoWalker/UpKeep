package com.upkeep.infrastructure.adapter.in.rest.pkg;

import com.upkeep.application.port.in.pkg.ImportPackagesFromLockfileUseCase;
import com.upkeep.application.port.in.pkg.ImportPackagesFromLockfileUseCase.ImportFromLockfileCommand;
import com.upkeep.application.port.in.pkg.ImportPackagesFromLockfileUseCase.ImportResult;
import com.upkeep.application.port.in.pkg.ImportPackagesFromListUseCase;
import com.upkeep.application.port.in.pkg.ImportPackagesFromListUseCase.ImportFromListCommand;
import com.upkeep.application.port.in.pkg.ImportPackagesFromListUseCase.ImportListResult;
import com.upkeep.application.port.in.pkg.ListCompanyPackagesUseCase;
import com.upkeep.application.port.in.pkg.ListCompanyPackagesUseCase.ListPackagesQuery;
import com.upkeep.application.port.in.pkg.ListCompanyPackagesUseCase.PackageListResult;
import com.upkeep.application.port.out.auth.TokenService;
import com.upkeep.application.port.out.auth.TokenService.TokenClaims;
import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiError;
import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/companies/{companyId}/packages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PackageResource {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final ImportPackagesFromLockfileUseCase importFromLockfileUseCase;
    private final ImportPackagesFromListUseCase importFromListUseCase;
    private final ListCompanyPackagesUseCase listPackagesUseCase;
    private final TokenService tokenService;

    @Inject
    public PackageResource(ImportPackagesFromLockfileUseCase importFromLockfileUseCase,
                           ImportPackagesFromListUseCase importFromListUseCase,
                           ListCompanyPackagesUseCase listPackagesUseCase,
                           TokenService tokenService) {
        this.importFromLockfileUseCase = importFromLockfileUseCase;
        this.importFromListUseCase = importFromListUseCase;
        this.listPackagesUseCase = listPackagesUseCase;
        this.tokenService = tokenService;
    }

    @GET
    public Response listPackages(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                                 @PathParam("companyId") String companyId,
                                 @QueryParam("search") String search,
                                 @QueryParam("page") @DefaultValue("0") int page,
                                 @QueryParam("size") @DefaultValue("50") int size) {
        TokenClaims claims = validateToken(accessToken);
        if (claims == null) {
            return unauthorizedResponse();
        }

        PackageListResult result = listPackagesUseCase.execute(
                new ListPackagesQuery(companyId, claims.userId(), search, page, size)
        );

        PackageListResponse response = new PackageListResponse(
                result.packages().stream()
                        .map(p -> new PackageListResponse.PackageItemResponse(
                                p.id(), p.name(), p.registry(), p.importedAt()))
                        .toList(),
                result.totalCount(),
                result.page(),
                result.size()
        );

        return Response.ok(ApiResponse.success(response)).build();
    }

    @POST
    @Path("/import/lockfile")
    public Response importFromLockfile(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                                       @PathParam("companyId") String companyId,
                                       ImportLockfileRequest request) {
        TokenClaims claims = validateToken(accessToken);
        if (claims == null) {
            return unauthorizedResponse();
        }

        ImportResult result = importFromLockfileUseCase.execute(
                new ImportFromLockfileCommand(companyId, claims.userId(),
                        request.fileContent(), request.filename())
        );

        ImportResultResponse response = new ImportResultResponse(
                result.importedCount(),
                result.skippedCount(),
                result.totalParsed(),
                result.importedNames(),
                result.skippedNames()
        );

        return Response.status(201)
                .entity(ApiResponse.success(response))
                .build();
    }

    @POST
    @Path("/import/list")
    public Response importFromList(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken,
                                   @PathParam("companyId") String companyId,
                                   ImportListRequest request) {
        TokenClaims claims = validateToken(accessToken);
        if (claims == null) {
            return unauthorizedResponse();
        }

        ImportListResult result = importFromListUseCase.execute(
                new ImportFromListCommand(companyId, claims.userId(), request.packageNames())
        );

        ImportListResultResponse response = new ImportListResultResponse(
                result.importedCount(),
                result.skippedCount(),
                result.invalidCount(),
                result.importedNames(),
                result.skippedNames(),
                result.invalidNames()
        );

        return Response.status(201)
                .entity(ApiResponse.success(response))
                .build();
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

