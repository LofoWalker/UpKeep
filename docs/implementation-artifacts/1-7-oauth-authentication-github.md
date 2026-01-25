# Story 1.7: OAuth Authentication (GitHub)

Status: done

## Story

As a **visitor**,
I want to sign up or log in using my GitHub account,
so that I can quickly access Upkeep without creating a password.

## Acceptance Criteria

1. **Given** I am on the login or registration page  
   **When** I click "Continue with GitHub"  
   **Then** I am redirected to GitHub OAuth consent screen

2. **Given** I authorize on GitHub  
   **When** I am redirected back to Upkeep  
   **Then** if no account exists, one is created with my GitHub email  
   **And** I am authenticated and redirected to onboarding (new) or dashboard (existing)

3. **Given** my GitHub email matches an existing email/password account  
   **When** I complete OAuth  
   **Then** my accounts are linked  
   **And** I can log in with either method

## Tasks / Subtasks

- [x] Task 1: Setup GitHub OAuth application
  - [x] 1.1: Document GitHub OAuth app creation
  - [x] 1.2: Add OAuth config to environment variables
  - [x] 1.3: Create OAuth configuration class

- [x] Task 2: Create OAuth use case (AC: #1, #2, #3)
  - [x] 2.1: Create `OAuthLoginUseCase` port interface
  - [x] 2.2: Implement OAuth flow logic
  - [x] 2.3: Handle account linking
  - [x] 2.4: Handle new account creation

- [x] Task 3: Create OAuth infrastructure (AC: #1, #2)
  - [x] 3.1: Create `GitHubOAuthAdapter`
  - [x] 3.2: Implement token exchange
  - [x] 3.3: Implement user info retrieval
  - [x] 3.4: Update User entity for OAuth providers

- [x] Task 4: Create REST endpoints (AC: #1, #2)
  - [x] 4.1: Create `/api/auth/oauth/github` redirect endpoint
  - [x] 4.2: Create `/api/auth/oauth/github/callback` endpoint
  - [x] 4.3: Handle state parameter for CSRF protection

- [x] Task 5: Frontend OAuth flow (AC: #1, #2)
  - [x] 5.1: Add "Continue with GitHub" button
  - [x] 5.2: Create OAuth callback page
  - [x] 5.3: Handle OAuth errors

## Dev Notes

### OAuth Flow

```
1. User clicks "Continue with GitHub"
2. Frontend redirects to /api/auth/oauth/github
3. Backend redirects to GitHub with client_id, redirect_uri, scope, state
4. User authorizes on GitHub
5. GitHub redirects to /api/auth/oauth/github/callback with code
6. Backend exchanges code for access token
7. Backend fetches user info from GitHub
8. Backend creates/links account and generates JWT
9. Backend redirects to frontend with auth cookies set
```

### Database Schema Updates

```sql
-- V3__add_oauth_providers_to_users.sql
CREATE TABLE user_oauth_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(provider, provider_user_id)
);

-- Only index on user_id; UNIQUE constraint already indexes (provider, provider_user_id)
CREATE INDEX idx_user_oauth_providers__user_id ON user_oauth_providers(user_id);
```

### OAuth Adapter

```java
package com.upkeep.infrastructure.adapter.out.oauth;

public interface OAuthProvider {
    String getAuthorizationUrl(String state);
    OAuthTokenResponse exchangeCode(String code);
    OAuthUserInfo getUserInfo(String accessToken);
}

@ApplicationScoped
@Named("github")
public class GitHubOAuthAdapter implements OAuthProvider {

    @ConfigProperty(name = "oauth.github.client-id")
    String clientId;

    @ConfigProperty(name = "oauth.github.client-secret")
    String clientSecret;

    @ConfigProperty(name = "oauth.github.redirect-uri")
    String redirectUri;

    private static final String AUTH_URL = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_URL = "https://api.github.com/user";
    private static final String EMAILS_URL = "https://api.github.com/user/emails";

    @Override
    public String getAuthorizationUrl(String state) {
        return AUTH_URL + "?" +
            "client_id=" + clientId +
            "&redirect_uri=" + URLEncoder.encode(redirectUri, UTF_8) +
            "&scope=user:email" +
            "&state=" + state;
    }

    @Override
    public OAuthTokenResponse exchangeCode(String code) {
        // POST to TOKEN_URL with code, client_id, client_secret
        // Return access_token
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        // GET USER_URL with Bearer token
        // GET EMAILS_URL to get primary email
        // Return OAuthUserInfo(id, email, name, avatarUrl)
    }
}

public record OAuthUserInfo(
    String providerId,
    String email,
    String name,
    String avatarUrl
) {}
```

### Use Case Implementation

```java
package com.upkeep.application.usecase;

@ApplicationScoped
public class OAuthLoginUseCaseImpl implements OAuthLoginUseCase {

    private final UserRepository userRepository;
    private final OAuthProviderRepository oauthProviderRepository;
    private final TokenService tokenService;

    @Override
    @Transactional
    public OAuthResult execute(OAuthCommand command) {
        // 1. Check if OAuth provider link exists
        Optional<UserOAuthProvider> existingLink = oauthProviderRepository
            .findByProviderAndProviderId(command.provider(), command.providerId());

        if (existingLink.isPresent()) {
            // Existing OAuth user - just login
            User user = userRepository.findById(existingLink.get().getUserId())
                .orElseThrow();
            return createAuthResult(user, false);
        }

        // 2. Check if email matches existing account
        Email email = new Email(command.email());
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // Link OAuth to existing account
            User user = existingUser.get();
            oauthProviderRepository.save(new UserOAuthProvider(
                user.getId(),
                command.provider(),
                command.providerId(),
                command.email()
            ));
            return createAuthResult(user, false);
        }

        // 3. Create new account
        User newUser = User.createFromOAuth(email, command.accountType());
        userRepository.save(newUser);
        oauthProviderRepository.save(new UserOAuthProvider(
            newUser.getId(),
            command.provider(),
            command.providerId(),
            command.email()
        ));

        return createAuthResult(newUser, true);
    }

    private OAuthResult createAuthResult(User user, boolean isNewUser) {
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        return new OAuthResult(accessToken, refreshToken, isNewUser);
    }
}
```

### REST Endpoints

```java
@Path("/api/auth/oauth")
public class OAuthResource {

    @Inject
    @Named("github")
    OAuthProvider githubProvider;

    @GET
    @Path("/github")
    public Response initiateGitHubOAuth(@QueryParam("accountType") AccountType accountType) {
        String state = generateState(accountType);
        stateStore.save(state, accountType, Duration.ofMinutes(10));
        
        String authUrl = githubProvider.getAuthorizationUrl(state);
        return Response.temporaryRedirect(URI.create(authUrl)).build();
    }

    @GET
    @Path("/github/callback")
    public Response handleGitHubCallback(
        @QueryParam("code") String code,
        @QueryParam("state") String state,
        @QueryParam("error") String error
    ) {
        if (error != null) {
            return redirectToFrontend("/login?error=oauth_denied");
        }

        // Validate state
        StateData stateData = stateStore.consume(state)
            .orElseThrow(() -> new UnauthorizedException("Invalid state"));

        // Exchange code for token
        OAuthTokenResponse tokenResponse = githubProvider.exchangeCode(code);
        OAuthUserInfo userInfo = githubProvider.getUserInfo(tokenResponse.accessToken());

        // Process OAuth login
        OAuthResult result = oauthLoginUseCase.execute(new OAuthCommand(
            "github",
            userInfo.providerId(),
            userInfo.email(),
            stateData.accountType()
        ));

        // Redirect with cookies
        String redirectPath = result.isNewUser() ? "/onboarding" : "/dashboard";
        return Response.temporaryRedirect(URI.create(frontendUrl + redirectPath))
            .cookie(createAccessTokenCookie(result.accessToken()))
            .cookie(createRefreshTokenCookie(result.refreshToken()))
            .build();
    }
}
```

### Frontend Implementation

```tsx
// apps/web/src/features/auth/components/OAuthButtons.tsx
export function OAuthButtons({ accountType }: { accountType?: AccountType }) {
  const handleGitHubLogin = () => {
    const params = accountType ? `?accountType=${accountType}` : ''
    window.location.href = `/api/auth/oauth/github${params}`
  }

  return (
    <div className="space-y-3">
      <Button
        variant="outline"
        className="w-full"
        onClick={handleGitHubLogin}
      >
        <GitHubIcon className="mr-2 h-4 w-4" />
        Continue with GitHub
      </Button>
    </div>
  )
}

// OAuth callback page
// apps/web/src/pages/auth/oauth/callback.tsx
export function OAuthCallbackPage() {
  const navigate = useNavigate()
  const searchParams = useSearchParams()
  const error = searchParams.get('error')

  useEffect(() => {
    if (error) {
      navigate('/login?error=' + error)
    }
    // Otherwise, cookies are set and we're redirected by backend
  }, [error])

  return <LoadingSpinner message="Completing sign in..." />
}
```

### Environment Variables

```bash
# OAuth - GitHub
OAUTH_GITHUB_CLIENT_ID=your_client_id
OAUTH_GITHUB_CLIENT_SECRET=your_client_secret
OAUTH_GITHUB_REDIRECT_URI=http://localhost:8080/api/auth/oauth/github/callback
```

### Security Notes

- State parameter MUST be validated to prevent CSRF
- Store state with expiry (10 minutes)
- Validate email from GitHub is verified
- Use HTTPS for all OAuth redirects in production

### Dependencies on Previous Stories

- Story 1.5: User registration (users table)
- Story 1.6: JWT token infrastructure

### References

- [Source: architecture.md#Authentication-Security] - OAuth patterns
- [Source: epics.md#Story-1.7] - Original acceptance criteria
- GitHub OAuth Docs: https://docs.github.com/en/apps/oauth-apps

## Dev Agent Record

### Agent Model Used

Claude (GitHub Copilot)

### Completion Notes List

- Implemented full OAuth flow for GitHub authentication
- Extended Customer domain model to support OAuth users (no password)
- Created hexagonal architecture compliant structure
- Frontend OAuth buttons added to login and register forms
- State parameter implemented for CSRF protection
- Account linking supported when email matches

### Change Log

**Backend:**
- `V3__create_user_oauth_providers_table.sql` - OAuth provider links table
- `V4__make_password_hash_nullable.sql` - Allow OAuth users without password
- `domain/model/oauth/` - OAuthProvider, UserOAuthProvider, OAuthUserInfo
- `domain/model/customer/Customer.java` - Added createFromOAuth(), Optional<PasswordHash>
- `application/port/in/OAuthLoginUseCase.java` - Use case interface
- `application/port/out/OAuthProviderAdapter.java` - OAuth provider port
- `application/port/out/UserOAuthProviderRepository.java` - Repository port
- `application/port/out/OAuthStateService.java` - State management port
- `application/usecase/OAuthLoginUseCaseImpl.java` - Use case implementation
- `infrastructure/adapter/out/persistence/UserOAuthProvider*` - Persistence layer
- `infrastructure/adapter/out/oauth/GitHubOAuthAdapter.java` - GitHub API integration
- `infrastructure/adapter/out/oauth/InMemoryOAuthStateService.java` - State management
- `infrastructure/adapter/in/rest/auth/OAuthResource.java` - REST endpoints

**Frontend:**
- `components/icons/GitHubIcon.tsx` - GitHub SVG icon
- `features/auth/OAuthButtons.tsx` - OAuth button component
- `features/auth/LoginForm.tsx` - Added OAuth buttons and error handling
- `features/auth/RegisterForm.tsx` - Added OAuth buttons

### File List

**Backend (apps/api/src/main/java/com/upkeep/):**
- domain/model/oauth/OAuthProvider.java
- domain/model/oauth/UserOAuthProvider.java
- domain/model/oauth/OAuthUserInfo.java
- application/port/in/OAuthLoginUseCase.java
- application/port/out/OAuthProviderAdapter.java
- application/port/out/UserOAuthProviderRepository.java
- application/port/out/OAuthStateService.java
- application/usecase/OAuthLoginUseCaseImpl.java
- infrastructure/adapter/out/persistence/UserOAuthProviderEntity.java
- infrastructure/adapter/out/persistence/UserOAuthProviderJpaRepository.java
- infrastructure/adapter/out/persistence/UserOAuthProviderMapper.java
- infrastructure/adapter/out/persistence/UserOAuthProviderRepositoryImpl.java
- infrastructure/adapter/out/oauth/GitHubOAuthAdapter.java
- infrastructure/adapter/out/oauth/InMemoryOAuthStateService.java
- infrastructure/adapter/in/rest/auth/OAuthResource.java

**Backend Resources:**
- db/migration/V3__create_user_oauth_providers_table.sql
- db/migration/V4__make_password_hash_nullable.sql

**Frontend (apps/web/src/):**
- components/icons/GitHubIcon.tsx
- components/icons/index.ts
- features/auth/OAuthButtons.tsx

**Modified Files:**
- domain/model/customer/Customer.java
- application/usecase/AuthenticateCustomerUseCaseImpl.java
- infrastructure/adapter/out/persistence/CustomerEntity.java
- infrastructure/adapter/out/persistence/CustomerMapper.java
- resources/application.properties
- features/auth/LoginForm.tsx
- features/auth/RegisterForm.tsx
- features/auth/index.ts

