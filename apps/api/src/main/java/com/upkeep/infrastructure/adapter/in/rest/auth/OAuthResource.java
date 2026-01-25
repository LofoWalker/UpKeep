package com.upkeep.infrastructure.adapter.in.rest.auth;

import com.upkeep.application.port.in.OAuthLoginUseCase;
import com.upkeep.application.port.out.OAuthProviderAdapter;
import com.upkeep.application.port.out.OAuthStateService;
import com.upkeep.domain.model.customer.AccountType;
import com.upkeep.domain.model.oauth.OAuthProvider;
import com.upkeep.domain.model.oauth.OAuthUserInfo;
import com.upkeep.infrastructure.adapter.out.oauth.GitHubOAuthAdapter;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;

@Path("/api/auth/oauth")
@Produces(MediaType.APPLICATION_JSON)
public class OAuthResource {

    private static final Logger LOG = Logger.getLogger(OAuthResource.class);

    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final OAuthProviderAdapter githubProvider;
    private final OAuthStateService stateService;
    private final OAuthLoginUseCase oauthLoginUseCase;

    @ConfigProperty(name = "app.frontend-url", defaultValue = "http://localhost:5173")
    String frontendUrl;

    @ConfigProperty(name = "jwt.access-token-expiry-seconds", defaultValue = "900")
    int accessTokenExpirySeconds;

    @ConfigProperty(name = "jwt.refresh-token-expiry-seconds", defaultValue = "604800")
    int refreshTokenExpirySeconds;

    @ConfigProperty(name = "app.use-secure-cookies", defaultValue = "true")
    boolean useSecureCookies;

    public OAuthResource(@Named("github") OAuthProviderAdapter githubProvider,
                         OAuthStateService stateService,
                         OAuthLoginUseCase oauthLoginUseCase) {
        this.githubProvider = githubProvider;
        this.stateService = stateService;
        this.oauthLoginUseCase = oauthLoginUseCase;
    }

    @GET
    @Path("/github")
    public Response initiateGitHubOAuth(@QueryParam("accountType") AccountType accountType) {
        AccountType type = accountType != null ? accountType : AccountType.COMPANY;
        String state = stateService.generateState(type);
        String authUrl = githubProvider.getAuthorizationUrl(state);
        return Response.temporaryRedirect(URI.create(authUrl)).build();
    }

    @GET
    @Path("/github/callback")
    public Response handleGitHubCallback(@QueryParam("code") String code,
                                         @QueryParam("state") String state,
                                         @QueryParam("error") String error,
                                         @QueryParam("error_description") String errorDescription) {

        if (error != null) {
            String desc = errorDescription != null ? errorDescription : "OAuth authorization denied";
            return redirectToFrontendWithError("oauth_denied", desc);
        }

        if (code == null || code.isBlank()) {
            return redirectToFrontendWithError("invalid_request", "Missing authorization code");
        }

        OAuthStateService.StateData stateData = stateService.consumeState(state)
                .orElse(null);

        if (stateData == null) {
            return redirectToFrontendWithError("invalid_state", "Invalid or expired state parameter");
        }

        try {
            OAuthProviderAdapter.OAuthTokenResponse tokenResponse = githubProvider.exchangeCode(code);
            OAuthUserInfo userInfo = githubProvider.getUserInfo(tokenResponse.accessToken());

            OAuthLoginUseCase.OAuthResult result = oauthLoginUseCase.execute(
                    new OAuthLoginUseCase.OAuthCommand(
                            OAuthProvider.GITHUB,
                            userInfo.providerUserId(),
                            userInfo.email(),
                            stateData.accountType()
                    )
            );

            String redirectPath = result.isNewUser() ? "/onboarding" : "/dashboard";

            return Response.temporaryRedirect(URI.create(frontendUrl + redirectPath))
                    .cookie(createAccessTokenCookie(result.accessToken()))
                    .cookie(createRefreshTokenCookie(result.refreshToken()))
                    .build();

        } catch (GitHubOAuthAdapter.OAuthException e) {
            LOG.errorf("GitHub OAuth error: %s", e.getMessage());
            return redirectToFrontendWithError("oauth_error", e.getMessage());
        } catch (Exception e) {
            LOG.errorf(e, "OAuth authentication failed unexpectedly");
            return redirectToFrontendWithError("server_error", "Authentication failed");
        }
    }

    private Response redirectToFrontendWithError(String code, String message) {
        String errorUrl = frontendUrl + "/login?error=" + code + "&message=" +
                java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8);
        return Response.temporaryRedirect(URI.create(errorUrl)).build();
    }

    private NewCookie createAccessTokenCookie(String token) {
        return new NewCookie.Builder(ACCESS_TOKEN_COOKIE)
                .value(token)
                .path("/")
                .httpOnly(true)
                .secure(useSecureCookies)
                .sameSite(NewCookie.SameSite.LAX)
                .maxAge(accessTokenExpirySeconds)
                .build();
    }

    private NewCookie createRefreshTokenCookie(String token) {
        return new NewCookie.Builder(REFRESH_TOKEN_COOKIE)
                .value(token)
                .path("/api/auth")
                .httpOnly(true)
                .secure(useSecureCookies)
                .sameSite(NewCookie.SameSite.LAX)
                .maxAge(refreshTokenExpirySeconds)
                .build();
    }
}
