# Story 1.6: User Login with Email/Password

Status: ready-for-dev

## Story

As a **registered user**,
I want to log in with my email and password,
so that I can access my account.

## Acceptance Criteria

1. **Given** I am on the login page  
   **When** I enter valid credentials  
   **And** I submit the form  
   **Then** I am authenticated  
   **And** I receive a JWT access token (stored in httpOnly cookie)  
   **And** I am redirected to the dashboard

2. **Given** I enter invalid credentials  
   **When** I submit the form  
   **Then** I see an error: "Invalid email or password"  
   **And** no specific field is highlighted (security best practice)

3. **Given** I am authenticated  
   **When** my session expires  
   **Then** I am prompted to log in again

4. **Given** I have a valid refresh token  
   **When** my access token expires  
   **Then** a new access token is automatically issued

## Tasks / Subtasks

- [ ] Task 1: Create authentication use case (AC: #1, #2)
  - [ ] 1.1: Create `AuthenticateUserUseCase` port interface
  - [ ] 1.2: Implement authentication logic
  - [ ] 1.3: Create `TokenService` port interface
  - [ ] 1.4: Implement JWT token generation

- [ ] Task 2: Create token infrastructure (AC: #1, #3, #4)
  - [ ] 2.1: Implement `JwtTokenService` adapter
  - [ ] 2.2: Configure JWT signing key and expiry
  - [ ] 2.3: Create refresh token storage (database)
  - [ ] 2.4: Implement token refresh endpoint

- [ ] Task 3: Create REST endpoints (AC: #1, #2, #4)
  - [ ] 3.1: Create `/api/auth/login` endpoint
  - [ ] 3.2: Create `/api/auth/refresh` endpoint
  - [ ] 3.3: Create `/api/auth/logout` endpoint
  - [ ] 3.4: Configure httpOnly cookie settings

- [ ] Task 4: Create frontend login (AC: #1, #2, #3)
  - [ ] 4.1: Create login page component
  - [ ] 4.2: Create login form
  - [ ] 4.3: Implement auth context/state
  - [ ] 4.4: Create protected route wrapper
  - [ ] 4.5: Handle token refresh automatically

## Dev Notes

### JWT Token Strategy

| Token | Storage | Expiry | Purpose |
|-------|---------|--------|---------|
| Access Token | httpOnly cookie | 15 min | API authentication |
| Refresh Token | httpOnly cookie + DB | 7 days | Token renewal |

### Use Case Implementation

```java
package com.upkeep.application.port.in;

public interface AuthenticateUserUseCase {
    AuthResult execute(AuthCommand command);

    record AuthCommand(String email, String password) {}
    record AuthResult(String accessToken, String refreshToken, UserInfo user) {}
    record UserInfo(String id, String email, AccountType accountType) {}
}

// Implementation
@ApplicationScoped
public class AuthenticateUserUseCaseImpl implements AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    @Override
    public AuthResult execute(AuthCommand command) {
        Email email = new Email(command.email());
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordHasher.verify(command.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        return new AuthResult(
            accessToken,
            refreshToken,
            new UserInfo(user.getId().toString(), email.value(), user.getAccountType())
        );
    }
}
```

### Token Service

```java
package com.upkeep.application.port.out;

public interface TokenService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    TokenClaims validateAccessToken(String token);
    RefreshResult refreshAccessToken(String refreshToken);
    void revokeRefreshToken(String refreshToken);
}

// JWT Implementation
@ApplicationScoped
public class JwtTokenService implements TokenService {
    
    @ConfigProperty(name = "jwt.secret")
    String secret;
    
    @ConfigProperty(name = "jwt.access-token-expiry", defaultValue = "900") // 15 min
    int accessTokenExpiry;
    
    @ConfigProperty(name = "jwt.refresh-token-expiry", defaultValue = "604800") // 7 days
    int refreshTokenExpiry;

    @Override
    public String generateAccessToken(User user) {
        return Jwt.issuer("upkeep")
            .subject(user.getId().toString())
            .claim("email", user.getEmail().value())
            .claim("accountType", user.getAccountType().name())
            .expiresIn(Duration.ofSeconds(accessTokenExpiry))
            .sign();
    }

    @Override
    public String generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        // Store in database with expiry
        refreshTokenRepository.save(new RefreshToken(
            token,
            user.getId(),
            Instant.now().plusSeconds(refreshTokenExpiry)
        ));
        return token;
    }
}
```

### REST Endpoints

```java
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        AuthResult result = authenticateUserUseCase.execute(
            new AuthCommand(request.email(), request.password())
        );

        return Response.ok(ApiResponse.success(new LoginResponse(result.user())))
            .cookie(createAccessTokenCookie(result.accessToken()))
            .cookie(createRefreshTokenCookie(result.refreshToken()))
            .build();
    }

    @POST
    @Path("/refresh")
    public Response refresh(@CookieParam("refresh_token") String refreshToken) {
        RefreshResult result = tokenService.refreshAccessToken(refreshToken);
        
        return Response.ok(ApiResponse.success("Token refreshed"))
            .cookie(createAccessTokenCookie(result.accessToken()))
            .build();
    }

    @POST
    @Path("/logout")
    public Response logout(@CookieParam("refresh_token") String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
        
        return Response.ok(ApiResponse.success("Logged out"))
            .cookie(expireAccessTokenCookie())
            .cookie(expireRefreshTokenCookie())
            .build();
    }

    private NewCookie createAccessTokenCookie(String token) {
        return new NewCookie.Builder("access_token")
            .value(token)
            .path("/")
            .httpOnly(true)
            .secure(true) // HTTPS only
            .sameSite(SameSite.STRICT)
            .maxAge(900) // 15 minutes
            .build();
    }
}
```

### Database Schema

```sql
-- V2__create_refresh_tokens_table.sql
CREATE TABLE refresh_tokens (
    token VARCHAR(255) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_refresh_tokens__user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens__expires_at ON refresh_tokens(expires_at);
```

### Frontend Auth Context

```tsx
// apps/web/src/features/auth/context/AuthContext.tsx
import { createContext, useContext, useState, useEffect } from 'react'

interface AuthContextType {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    // Check if user is authenticated on mount
    checkAuth()
  }, [])

  const login = async (email: string, password: string) => {
    const response = await api.post('/auth/login', { email, password })
    setUser(response.data.user)
  }

  const logout = async () => {
    await api.post('/auth/logout')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}
```

### Protected Route

```tsx
// apps/web/src/features/auth/components/ProtectedRoute.tsx
export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      navigate('/login')
    }
  }, [isAuthenticated, isLoading])

  if (isLoading) return <LoadingSpinner />
  if (!isAuthenticated) return null

  return <>{children}</>
}
```

### Security Notes (NFR3)

- All cookies MUST be httpOnly (prevents XSS access)
- All cookies MUST be secure (HTTPS only)
- Use SameSite=Strict to prevent CSRF
- Never expose refresh tokens in API responses (only cookies)
- Implement rate limiting on login endpoint

### Dependencies on Previous Stories

- Story 1.4: API envelope pattern
- Story 1.5: User registration (users table exists)

### References

- [Source: architecture.md#Authentication-Security] - JWT strategy
- [Source: epics.md#Story-1.6] - Original acceptance criteria
- NFR3: TLS everywhere

## Dev Agent Record

### Agent Model Used

_To be filled by dev agent_

### Completion Notes List

_To be filled during implementation_

### Change Log

_To be filled during implementation_

### File List

_To be filled after implementation_

