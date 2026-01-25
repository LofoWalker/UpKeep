package com.upkeep.infrastructure.adapter.in.rest.auth;

import com.upkeep.application.port.in.OAuthLoginUseCase;
import com.upkeep.application.port.out.oauth.OAuthProviderAdapter;
import com.upkeep.application.port.out.oauth.OAuthStateService;
import com.upkeep.domain.model.customer.AccountType;
import com.upkeep.domain.model.oauth.OAuthUserInfo;
import com.upkeep.infrastructure.adapter.out.oauth.GitHubOAuthAdapter;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Named;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class OAuthResourceTest {

    private static final String VALID_STATE = "valid-state-token";
    private static final String VALID_CODE = "valid-auth-code";
    private static final String MOCK_ACCESS_TOKEN = "mock-github-access-token";
    private static final String MOCK_JWT_ACCESS = "mock.jwt.access";
    private static final String MOCK_JWT_REFRESH = "mock.jwt.refresh";

    @InjectMock
    @Named("github")
    OAuthProviderAdapter githubProvider;

    @InjectMock
    OAuthStateService stateService;

    @InjectMock
    OAuthLoginUseCase oauthLoginUseCase;

    @BeforeEach
    void setUp() {
        Mockito.reset(githubProvider, stateService, oauthLoginUseCase);
    }

    @Test
    void initiateGitHubOAuth_shouldRedirectToAuthorizationUrl() {
        String expectedAuthUrl = "https://github.com/login/oauth/authorize?client_id=test&state=" + VALID_STATE;
        when(stateService.generateState(AccountType.COMPANY)).thenReturn(VALID_STATE);
        when(githubProvider.getAuthorizationUrl(VALID_STATE)).thenReturn(expectedAuthUrl);

        given()
                .redirects().follow(false)
                .when()
                .get("/api/auth/oauth/github")
                .then()
                .statusCode(307)
                .header("Location", expectedAuthUrl);
    }

    @Test
    void initiateGitHubOAuth_withMaintainerAccountType_shouldPassAccountTypeToStateService() {
        String expectedAuthUrl = "https://github.com/login/oauth/authorize?state=" + VALID_STATE;
        when(stateService.generateState(AccountType.MAINTAINER)).thenReturn(VALID_STATE);
        when(githubProvider.getAuthorizationUrl(VALID_STATE)).thenReturn(expectedAuthUrl);

        given()
                .redirects().follow(false)
                .queryParam("accountType", "MAINTAINER")
                .when()
                .get("/api/auth/oauth/github")
                .then()
                .statusCode(307)
                .header("Location", expectedAuthUrl);
    }

    @Test
    void callback_withValidCode_forNewUser_shouldSetCookiesAndRedirectToOnboarding() {
        setupValidOAuthFlow(true);

        given()
                .redirects().follow(false)
                .queryParam("code", VALID_CODE)
                .queryParam("state", VALID_STATE)
                .when()
                .get("/api/auth/oauth/github/callback")
                .then()
                .statusCode(307)
                .header("Location", containsString("/onboarding"))
                .cookie("access_token", notNullValue())
                .cookie("refresh_token", notNullValue());
    }

    @Test
    void callback_withValidCode_forExistingUser_shouldSetCookiesAndRedirectToDashboard() {
        setupValidOAuthFlow(false);

        given()
                .redirects().follow(false)
                .queryParam("code", VALID_CODE)
                .queryParam("state", VALID_STATE)
                .when()
                .get("/api/auth/oauth/github/callback")
                .then()
                .statusCode(307)
                .header("Location", containsString("/dashboard"))
                .cookie("access_token", notNullValue())
                .cookie("refresh_token", notNullValue());
    }

    @Test
    void callback_withInvalidState_shouldRedirectWithError() {
        when(stateService.consumeState(anyString())).thenReturn(Optional.empty());

        given()
                .redirects().follow(false)
                .queryParam("code", VALID_CODE)
                .queryParam("state", "invalid-state")
                .when()
                .get("/api/auth/oauth/github/callback")
                .then()
                .statusCode(307)
                .header("Location", containsString("error=invalid_state"));
    }

    @Test
    void callback_withMissingCode_shouldRedirectWithError() {
        given()
                .redirects().follow(false)
                .queryParam("state", VALID_STATE)
                .when()
                .get("/api/auth/oauth/github/callback")
                .then()
                .statusCode(307)
                .header("Location", containsString("error=invalid_request"));
    }

    @Test
    void callback_withEmptyCode_shouldRedirectWithError() {
        given()
                .redirects().follow(false)
                .queryParam("code", "")
                .queryParam("state", VALID_STATE)
                .when()
                .get("/api/auth/oauth/github/callback")
                .then()
                .statusCode(307)
                .header("Location", containsString("error=invalid_request"));
    }

    @Test
    void callback_withProviderError_shouldRedirectWithOAuthDeniedError() {
        given()
                .redirects().follow(false)
                .queryParam("error", "access_denied")
                .queryParam("error_description", "User denied access")
                .queryParam("state", VALID_STATE)
                .when()
                .get("/api/auth/oauth/github/callback")
                .then()
                .statusCode(307)
                .header("Location", containsString("error=oauth_denied"))
                .header("Location", containsString("User+denied+access"));
    }

    @Test
    void callback_withProviderErrorWithoutDescription_shouldUseDefaultMessage() {
        given()
                .redirects().follow(false)
                .queryParam("error", "access_denied")
                .queryParam("state", VALID_STATE)
                .when()
                .get("/api/auth/oauth/github/callback")
                .then()
                .statusCode(307)
                .header("Location", containsString("error=oauth_denied"))
                .header("Location", containsString("OAuth+authorization+denied"));
    }

    @Test
    void callback_whenCodeExchangeFails_shouldRedirectWithOAuthError() {
        when(stateService.consumeState(VALID_STATE))
                .thenReturn(Optional.of(new OAuthStateService.StateData(AccountType.COMPANY)));
        when(githubProvider.exchangeCode(VALID_CODE))
                .thenThrow(new GitHubOAuthAdapter.OAuthException("Failed to exchange code"));

        given()
                .redirects().follow(false)
                .queryParam("code", VALID_CODE)
                .queryParam("state", VALID_STATE)
                .when()
                .get("/api/auth/oauth/github/callback")
                .then()
                .statusCode(307)
                .header("Location", containsString("error=oauth_error"));
    }

    @Test
    void callback_whenGetUserInfoFails_shouldRedirectWithOAuthError() {
        when(stateService.consumeState(VALID_STATE))
                .thenReturn(Optional.of(new OAuthStateService.StateData(AccountType.COMPANY)));
        when(githubProvider.exchangeCode(VALID_CODE))
                .thenReturn(new OAuthProviderAdapter.OAuthTokenResponse(MOCK_ACCESS_TOKEN, "Bearer", "user:email"));
        when(githubProvider.getUserInfo(MOCK_ACCESS_TOKEN))
                .thenThrow(new GitHubOAuthAdapter.OAuthException("Failed to get user info"));

        given()
                .redirects().follow(false)
                .queryParam("code", VALID_CODE)
                .queryParam("state", VALID_STATE)
                .when()
                .get("/api/auth/oauth/github/callback")
                .then()
                .statusCode(307)
                .header("Location", containsString("error=oauth_error"));
    }

    @Test
    void callback_whenUnexpectedErrorOccurs_shouldRedirectWithServerError() {
        when(stateService.consumeState(VALID_STATE))
                .thenReturn(Optional.of(new OAuthStateService.StateData(AccountType.COMPANY)));
        when(githubProvider.exchangeCode(VALID_CODE))
                .thenThrow(new RuntimeException("Unexpected error"));

        given()
                .redirects().follow(false)
                .queryParam("code", VALID_CODE)
                .queryParam("state", VALID_STATE)
                .when()
                .get("/api/auth/oauth/github/callback")
                .then()
                .statusCode(307)
                .header("Location", containsString("error=server_error"));
    }

    @Test
    void callback_withMaintainerAccountType_shouldCreateMaintainerAccount() {
        OAuthUserInfo userInfo = new OAuthUserInfo("gh-123", "maintainer@example.com", "Test User", null);

        when(stateService.consumeState(VALID_STATE))
                .thenReturn(Optional.of(new OAuthStateService.StateData(AccountType.MAINTAINER)));
        when(githubProvider.exchangeCode(VALID_CODE))
                .thenReturn(new OAuthProviderAdapter.OAuthTokenResponse(MOCK_ACCESS_TOKEN, "Bearer", "user:email"));
        when(githubProvider.getUserInfo(MOCK_ACCESS_TOKEN))
                .thenReturn(userInfo);
        when(oauthLoginUseCase.execute(any(OAuthLoginUseCase.OAuthCommand.class)))
                .thenReturn(new OAuthLoginUseCase.OAuthResult(
                        MOCK_JWT_ACCESS,
                        MOCK_JWT_REFRESH,
                        true,
                        "user-id-123",
                        "maintainer@example.com",
                        AccountType.MAINTAINER
                ));

        given()
                .redirects().follow(false)
                .queryParam("code", VALID_CODE)
                .queryParam("state", VALID_STATE)
                .when()
                .get("/api/auth/oauth/github/callback")
                .then()
                .statusCode(307)
                .header("Location", containsString("/onboarding"))
                .cookie("access_token", notNullValue())
                .cookie("refresh_token", notNullValue());
    }

    private void setupValidOAuthFlow(boolean isNewUser) {
        OAuthUserInfo userInfo = new OAuthUserInfo("gh-123", "test@example.com", "Test User", null);

        when(stateService.consumeState(VALID_STATE))
                .thenReturn(Optional.of(new OAuthStateService.StateData(AccountType.COMPANY)));
        when(githubProvider.exchangeCode(VALID_CODE))
                .thenReturn(new OAuthProviderAdapter.OAuthTokenResponse(MOCK_ACCESS_TOKEN, "Bearer", "user:email"));
        when(githubProvider.getUserInfo(MOCK_ACCESS_TOKEN))
                .thenReturn(userInfo);
        when(oauthLoginUseCase.execute(any(OAuthLoginUseCase.OAuthCommand.class)))
                .thenReturn(new OAuthLoginUseCase.OAuthResult(
                        MOCK_JWT_ACCESS,
                        MOCK_JWT_REFRESH,
                        isNewUser,
                        "user-id-123",
                        "test@example.com",
                        AccountType.COMPANY
                ));
    }
}
