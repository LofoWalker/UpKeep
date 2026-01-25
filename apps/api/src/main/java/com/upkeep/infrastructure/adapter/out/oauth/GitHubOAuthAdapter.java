package com.upkeep.infrastructure.adapter.out.oauth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upkeep.application.port.out.OAuthProviderAdapter;
import com.upkeep.domain.model.oauth.OAuthProvider;
import com.upkeep.domain.model.oauth.OAuthUserInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Named("github")
public class GitHubOAuthAdapter implements OAuthProviderAdapter {

    private static final String AUTH_URL = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_URL = "https://api.github.com/user";
    private static final String EMAILS_URL = "https://api.github.com/user/emails";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @ConfigProperty(name = "oauth.github.client-id")
    String clientId;

    @ConfigProperty(name = "oauth.github.client-secret")
    String clientSecret;

    @ConfigProperty(name = "oauth.github.redirect-uri")
    String redirectUri;

    @Inject
    public GitHubOAuthAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.GITHUB;
    }

    @Override
    public String getAuthorizationUrl(String state) {
        return AUTH_URL + "?" +
                "client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=user:email" +
                "&state=" + state;
    }

    @Override
    public OAuthTokenResponse exchangeCode(String code) {
        String formBody = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new OAuthException("Failed to exchange code for token: " + response.statusCode());
            }

            Map<String, Object> body = objectMapper.readValue(response.body(), new TypeReference<>() {});

            if (body.containsKey("error")) {
                throw new OAuthException("OAuth error: " + body.get("error_description"));
            }

            return new OAuthTokenResponse(
                    (String) body.get("access_token"),
                    (String) body.get("token_type"),
                    (String) body.get("scope")
            );
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OAuthException("Failed to exchange code: " + e.getMessage());
        }
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USER_URL))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new OAuthException("Failed to get user info: " + response.statusCode());
            }

            Map<String, Object> userInfo = objectMapper.readValue(response.body(), new TypeReference<>() {});

            String email = (String) userInfo.get("email");

            if (email == null) {
                email = fetchPrimaryEmail(accessToken);
            }

            if (email == null) {
                throw new OAuthException("Could not retrieve email from GitHub");
            }

            String id = String.valueOf(userInfo.get("id"));
            String name = (String) userInfo.get("name");
            String avatarUrl = (String) userInfo.get("avatar_url");

            return new OAuthUserInfo(id, email, name, avatarUrl);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OAuthException("Failed to get user info: " + e.getMessage());
        }
    }

    private String fetchPrimaryEmail(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EMAILS_URL))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            List<Map<String, Object>> emails = objectMapper.readValue(response.body(), new TypeReference<>() {});

            return emails.stream()
                    .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
                    .map(e -> (String) e.get("email"))
                    .findFirst()
                    .orElse(null);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public static class OAuthException extends RuntimeException {
        public OAuthException(String message) {
            super(message);
        }
    }
}
