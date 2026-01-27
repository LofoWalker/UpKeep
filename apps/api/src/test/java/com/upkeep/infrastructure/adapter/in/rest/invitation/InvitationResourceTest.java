package com.upkeep.infrastructure.adapter.in.rest.invitation;

import com.upkeep.application.port.in.AcceptInvitationUseCase;
import com.upkeep.application.port.in.GetInvitationUseCase;
import com.upkeep.application.port.out.auth.TokenService;
import com.upkeep.domain.exception.AlreadyMemberException;
import com.upkeep.domain.exception.InvitationExpiredException;
import com.upkeep.domain.exception.InvitationNotFoundException;
import com.upkeep.domain.model.invitation.InvitationStatus;
import com.upkeep.domain.model.membership.Role;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("InvitationResource Integration Tests")
class InvitationResourceTest {

    private static final String VALID_TOKEN = "valid-invitation-token";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String EXPIRED_TOKEN = "expired-token";
    private static final String VALID_ACCESS_TOKEN = "valid.access.token";
    private static final String CUSTOMER_ID = UUID.randomUUID().toString();
    private static final String COMPANY_ID = UUID.randomUUID().toString();
    private static final String INVITATION_ID = UUID.randomUUID().toString();
    private static final String MEMBERSHIP_ID = UUID.randomUUID().toString();

    @InjectMock
    GetInvitationUseCase getInvitationUseCase;

    @InjectMock
    AcceptInvitationUseCase acceptInvitationUseCase;

    @InjectMock
    TokenService tokenService;

    @BeforeEach
    void setUp() {
        Mockito.reset(getInvitationUseCase, acceptInvitationUseCase, tokenService);
    }

    @Nested
    @DisplayName("GET /api/invitations/{token}")
    class GetInvitationTests {

        @Test
        @DisplayName("should return invitation details for valid token")
        void shouldReturnInvitationDetailsForValidToken() {
            var details = new GetInvitationUseCase.InvitationDetails(
                    INVITATION_ID,
                    "Test Company",
                    Role.MEMBER,
                    InvitationStatus.PENDING,
                    false,
                    Instant.now().plus(7, ChronoUnit.DAYS)
            );
            when(getInvitationUseCase.execute(any(GetInvitationUseCase.GetInvitationQuery.class)))
                    .thenReturn(details);

            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/invitations/{token}", VALID_TOKEN)
                    .then()
                    .statusCode(200)
                    .body("data.id", equalTo(INVITATION_ID))
                    .body("data.companyName", equalTo("Test Company"))
                    .body("data.role", equalTo("MEMBER"))
                    .body("data.status", equalTo("PENDING"))
                    .body("data.isExpired", equalTo(false))
                    .body("data.expiresAt", notNullValue())
                    .body("error", nullValue());
        }

        @Test
        @DisplayName("should return expired invitation details")
        void shouldReturnExpiredInvitationDetails() {
            var details = new GetInvitationUseCase.InvitationDetails(
                    INVITATION_ID,
                    "Test Company",
                    Role.OWNER,
                    InvitationStatus.PENDING,
                    true,
                    Instant.now().minus(1, ChronoUnit.DAYS)
            );
            when(getInvitationUseCase.execute(any(GetInvitationUseCase.GetInvitationQuery.class)))
                    .thenReturn(details);

            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/invitations/{token}", EXPIRED_TOKEN)
                    .then()
                    .statusCode(200)
                    .body("data.isExpired", equalTo(true))
                    .body("data.status", equalTo("PENDING"))
                    .body("error", nullValue());
        }

        @Test
        @DisplayName("should return 404 for non-existent token")
        void shouldReturn404ForNonExistentToken() {
            when(getInvitationUseCase.execute(any(GetInvitationUseCase.GetInvitationQuery.class)))
                    .thenThrow(new InvitationNotFoundException(INVALID_TOKEN));

            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/invitations/{token}", INVALID_TOKEN)
                    .then()
                    .statusCode(404)
                    .body("error.code", equalTo("INVITATION_NOT_FOUND"))
                    .body("error.message", notNullValue())
                    .body("error.traceId", notNullValue());
        }

        @Test
        @DisplayName("should return accepted invitation status")
        void shouldReturnAcceptedInvitationStatus() {
            var details = new GetInvitationUseCase.InvitationDetails(
                    INVITATION_ID,
                    "Test Company",
                    Role.MEMBER,
                    InvitationStatus.ACCEPTED,
                    false,
                    Instant.now().plus(7, ChronoUnit.DAYS)
            );
            when(getInvitationUseCase.execute(any(GetInvitationUseCase.GetInvitationQuery.class)))
                    .thenReturn(details);

            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/invitations/{token}", VALID_TOKEN)
                    .then()
                    .statusCode(200)
                    .body("data.status", equalTo("ACCEPTED"));
        }
    }

    @Nested
    @DisplayName("POST /api/invitations/{token}/accept")
    class AcceptInvitationTests {

        @Test
        @DisplayName("should accept invitation with valid auth token")
        void shouldAcceptInvitationWithValidAuthToken() {
            setupValidAuthentication();

            var result = new AcceptInvitationUseCase.AcceptInvitationResult(
                    COMPANY_ID,
                    "Test Company",
                    "test-company",
                    MEMBERSHIP_ID,
                    Role.MEMBER
            );
            when(acceptInvitationUseCase.execute(any(AcceptInvitationUseCase.AcceptInvitationCommand.class)))
                    .thenReturn(result);

            given()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", VALID_ACCESS_TOKEN)
                    .when()
                    .post("/api/invitations/{token}/accept", VALID_TOKEN)
                    .then()
                    .statusCode(200)
                    .body("data.companyId", equalTo(COMPANY_ID))
                    .body("data.companyName", equalTo("Test Company"))
                    .body("data.companySlug", equalTo("test-company"))
                    .body("data.membershipId", equalTo(MEMBERSHIP_ID))
                    .body("data.role", equalTo("MEMBER"))
                    .body("error", nullValue());
        }

        @Test
        @DisplayName("should return 401 when no access token provided")
        void shouldReturn401WhenNoAccessToken() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/invitations/{token}/accept", VALID_TOKEN)
                    .then()
                    .statusCode(401)
                    .body("error.code", equalTo("UNAUTHORIZED"))
                    .body("error.message", equalTo("Authentication required"));
        }

        @Test
        @DisplayName("should return 401 when access token is empty")
        void shouldReturn401WhenAccessTokenEmpty() {
            given()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", "")
                    .when()
                    .post("/api/invitations/{token}/accept", VALID_TOKEN)
                    .then()
                    .statusCode(401)
                    .body("error.code", equalTo("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("should return 401 when access token is invalid")
        void shouldReturn401WhenAccessTokenInvalid() {
            when(tokenService.validateAccessToken("invalid-token"))
                    .thenThrow(new RuntimeException("Invalid token"));

            given()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", "invalid-token")
                    .when()
                    .post("/api/invitations/{token}/accept", VALID_TOKEN)
                    .then()
                    .statusCode(401)
                    .body("error.code", equalTo("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("should return 404 for non-existent invitation token")
        void shouldReturn404ForNonExistentInvitationToken() {
            setupValidAuthentication();

            when(acceptInvitationUseCase.execute(any(AcceptInvitationUseCase.AcceptInvitationCommand.class)))
                    .thenThrow(new InvitationNotFoundException(INVALID_TOKEN));

            given()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", VALID_ACCESS_TOKEN)
                    .when()
                    .post("/api/invitations/{token}/accept", INVALID_TOKEN)
                    .then()
                    .statusCode(404)
                    .body("error.code", equalTo("INVITATION_NOT_FOUND"))
                    .body("error.traceId", notNullValue());
        }

        @Test
        @DisplayName("should return 410 for expired invitation")
        void shouldReturn410ForExpiredInvitation() {
            setupValidAuthentication();

            when(acceptInvitationUseCase.execute(any(AcceptInvitationUseCase.AcceptInvitationCommand.class)))
                    .thenThrow(new InvitationExpiredException());

            given()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", VALID_ACCESS_TOKEN)
                    .when()
                    .post("/api/invitations/{token}/accept", EXPIRED_TOKEN)
                    .then()
                    .statusCode(410)
                    .body("error.code", equalTo("INVITATION_EXPIRED"))
                    .body("error.traceId", notNullValue());
        }

        @Test
        @DisplayName("should return 409 when user is already a member")
        void shouldReturn409WhenAlreadyMember() {
            setupValidAuthentication();

            when(acceptInvitationUseCase.execute(any(AcceptInvitationUseCase.AcceptInvitationCommand.class)))
                    .thenThrow(new AlreadyMemberException());

            given()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", VALID_ACCESS_TOKEN)
                    .when()
                    .post("/api/invitations/{token}/accept", VALID_TOKEN)
                    .then()
                    .statusCode(409)
                    .body("error.code", equalTo("ALREADY_MEMBER"))
                    .body("error.traceId", notNullValue());
        }

        @Test
        @DisplayName("should accept invitation with owner role")
        void shouldAcceptInvitationWithOwnerRole() {
            setupValidAuthentication();

            var result = new AcceptInvitationUseCase.AcceptInvitationResult(
                    COMPANY_ID,
                    "Owner Company",
                    "owner-company",
                    MEMBERSHIP_ID,
                    Role.OWNER
            );
            when(acceptInvitationUseCase.execute(any(AcceptInvitationUseCase.AcceptInvitationCommand.class)))
                    .thenReturn(result);

            given()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", VALID_ACCESS_TOKEN)
                    .when()
                    .post("/api/invitations/{token}/accept", VALID_TOKEN)
                    .then()
                    .statusCode(200)
                    .body("data.role", equalTo("OWNER"));
        }
    }

    private void setupValidAuthentication() {
        when(tokenService.validateAccessToken(VALID_ACCESS_TOKEN))
                .thenReturn(new TokenService.TokenClaims(CUSTOMER_ID, "test@example.com", "COMPANY"));
    }
}
