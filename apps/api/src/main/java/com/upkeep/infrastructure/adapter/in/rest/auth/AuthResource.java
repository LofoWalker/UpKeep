package com.upkeep.infrastructure.adapter.in.rest.auth;

import com.upkeep.application.port.in.AuthenticateCustomerUseCase;
import com.upkeep.application.port.in.AuthenticateCustomerUseCase.AuthCommand;
import com.upkeep.application.port.in.AuthenticateCustomerUseCase.AuthResult;
import com.upkeep.application.port.in.RegisterCustomerUseCase;
import com.upkeep.application.port.out.auth.TokenService;
import com.upkeep.application.port.out.auth.TokenService.RefreshResult;
import com.upkeep.application.port.out.auth.TokenService.TokenClaims;
import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiError;
import com.upkeep.infrastructure.adapter.in.rest.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final AuthenticateCustomerUseCase authenticateCustomerUseCase;
    private final TokenService tokenService;

    @ConfigProperty(name = "jwt.access-token-expiry-seconds", defaultValue = "900")
    int accessTokenExpirySeconds;

    @ConfigProperty(name = "jwt.refresh-token-expiry-seconds", defaultValue = "604800")
    int refreshTokenExpirySeconds;

    @ConfigProperty(name = "app.use-secure-cookies", defaultValue = "true")
    boolean useSecureCookies;

    public AuthResource(RegisterCustomerUseCase registerCustomerUseCase,
                        AuthenticateCustomerUseCase authenticateCustomerUseCase,
                        TokenService tokenService) {
        this.registerCustomerUseCase = registerCustomerUseCase;
        this.authenticateCustomerUseCase = authenticateCustomerUseCase;
        this.tokenService = tokenService;
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

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        AuthResult result = authenticateCustomerUseCase.execute(
                new AuthCommand(request.email(), request.password())
        );

        LoginResponse loginResponse = new LoginResponse(
                result.user().id(),
                result.user().email(),
                result.user().accountType()
        );

        return Response.ok(ApiResponse.success(loginResponse))
                .cookie(createAccessTokenCookie(result.accessToken()))
                .cookie(createRefreshTokenCookie(result.refreshToken()))
                .build();
    }

    @POST
    @Path("/refresh")
    public Response refresh(@CookieParam(REFRESH_TOKEN_COOKIE) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Response.status(401)
                    .entity(ApiResponse.error(new ApiError(
                            "INVALID_TOKEN", "No refresh token provided", null, null)))
                    .build();
        }

        RefreshResult result = tokenService.refreshAccessToken(refreshToken);

        return Response.ok(ApiResponse.success("Token refreshed"))
                .cookie(createAccessTokenCookie(result.accessToken()))
                .build();
    }

    @POST
    @Path("/logout")
    public Response logout(@CookieParam(REFRESH_TOKEN_COOKIE) String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenService.revokeRefreshToken(refreshToken);
        }

        return Response.ok(ApiResponse.success("Logged out"))
                .cookie(expireCookie(ACCESS_TOKEN_COOKIE))
                .cookie(expireCookie(REFRESH_TOKEN_COOKIE))
                .build();
    }

    @GET
    @Path("/me")
    public Response getCurrentUser(@CookieParam(ACCESS_TOKEN_COOKIE) String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return Response.status(401)
                    .entity(ApiResponse.error(new ApiError(
                            "UNAUTHORIZED", "Not authenticated", null, null)))
                    .build();
        }

        try {
            TokenClaims claims = tokenService.validateAccessToken(accessToken);
            MeResponse response = new MeResponse(claims.userId(), claims.email(), claims.accountType());
            return Response.ok(ApiResponse.success(response)).build();
        } catch (Exception e) {
            return Response.status(401)
                    .entity(ApiResponse.error(new ApiError(
                            "INVALID_TOKEN", "Invalid or expired token", null, null)))
                    .build();
        }
    }

    private NewCookie createAccessTokenCookie(String token) {
        return new NewCookie.Builder(ACCESS_TOKEN_COOKIE)
                .value(token)
                .path("/")
                .httpOnly(true)
                .secure(isSecureCookie())
                .sameSite(NewCookie.SameSite.STRICT)
                .maxAge(accessTokenExpirySeconds)
                .build();
    }

    private NewCookie createRefreshTokenCookie(String token) {
        return new NewCookie.Builder(REFRESH_TOKEN_COOKIE)
                .value(token)
                .path("/api/auth")
                .httpOnly(true)
                .secure(isSecureCookie())
                .sameSite(NewCookie.SameSite.STRICT)
                .maxAge(refreshTokenExpirySeconds)
                .build();
    }

    private NewCookie expireCookie(String name) {
        String path = REFRESH_TOKEN_COOKIE.equals(name) ? "/api/auth" : "/";
        return new NewCookie.Builder(name)
                .value("")
                .path(path)
                .httpOnly(true)
                .secure(isSecureCookie())
                .sameSite(NewCookie.SameSite.STRICT)
                .maxAge(0)
                .build();
    }

    private boolean isSecureCookie() {
        return useSecureCookies;
    }
}
