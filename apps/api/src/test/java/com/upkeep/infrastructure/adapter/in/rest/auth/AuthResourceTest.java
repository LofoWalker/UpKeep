package com.upkeep.infrastructure.adapter.in.rest.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AuthResourceTest {

    @Test
    void shouldRegisterNewCustomer() {
        String requestBody = """
                {
                    "email": "newuser@example.com",
                    "password": "SecurePass123",
                    "confirmPassword": "SecurePass123",
                    "accountType": "COMPANY"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("data.email", equalTo("newuser@example.com"))
                .body("data.accountType", equalTo("COMPANY"))
                .body("data.customerId", notNullValue())
                .body("meta.timestamp", notNullValue())
                .body("error", nullValue());
    }

    @Test
    void shouldRejectRegistrationWhenPasswordsDoNotMatch() {
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "SecurePass123",
                    "confirmPassword": "DifferentPass123",
                    "accountType": "COMPANY"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("error.code", equalTo("VALIDATION_ERROR"))
                .body("error.message", equalTo("Passwords do not match"));
    }

    @Test
    void shouldRejectRegistrationWithInvalidEmail() {
        String requestBody = """
                {
                    "email": "invalid-email",
                    "password": "SecurePass123",
                    "confirmPassword": "SecurePass123",
                    "accountType": "COMPANY"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("error.code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectRegistrationWithWeakPassword() {
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "weak",
                    "confirmPassword": "weak",
                    "accountType": "COMPANY"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("error.code", equalTo("VALIDATION_ERROR"))
                .body("error.details", notNullValue());
    }

    @Test
    void shouldRejectDuplicateEmailRegistration() {
        String email = "duplicate@example.com";
        String requestBody = """
                {
                    "email": "%s",
                    "password": "SecurePass123",
                    "confirmPassword": "SecurePass123",
                    "accountType": "MAINTAINER"
                }
                """.formatted(email);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(409)
                .body("error.code", equalTo("CUSTOMER_ALREADY_EXISTS"))
                .body("error.message", containsString("already exists"));
    }

    @Test
    void shouldLoginWithValidCredentials() {
        String email = "logintest@example.com";
        String password = "SecurePass123";

        registerTestUser(email, password);

        String loginBody = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("data.email", equalTo(email))
                .body("data.customerId", notNullValue())
                .cookie("access_token", notNullValue())
                .cookie("refresh_token", notNullValue());
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() {
        String email = "loginbad@example.com";
        String password = "SecurePass123";

        registerTestUser(email, password);

        String loginBody = """
                {
                    "email": "%s",
                    "password": "WrongPassword123"
                }
                """.formatted(email);

        given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("error.code", equalTo("INVALID_CREDENTIALS"))
                .body("error.message", equalTo("Invalid email or password"));
    }

    @Test
    void shouldRejectLoginWithNonExistentEmail() {
        String loginBody = """
                {
                    "email": "nonexistent@example.com",
                    "password": "Password123"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("error.code", equalTo("INVALID_CREDENTIALS"))
                .body("error.message", equalTo("Invalid email or password"));
    }

    @Test
    void shouldRefreshAccessToken() {
        String email = "refreshtest@example.com";
        String password = "SecurePass123";

        registerTestUser(email, password);

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "email": "%s",
                            "password": "%s"
                        }
                        """.formatted(email, password))
                .when()
                .post("/api/auth/login");

        String refreshToken = loginResponse.getCookie("refresh_token");

        given()
                .contentType(ContentType.JSON)
                .cookie("refresh_token", refreshToken)
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(200)
                .cookie("access_token", notNullValue());
    }

    @Test
    void shouldRejectRefreshWithNoToken() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(401)
                .body("error.code", equalTo("INVALID_TOKEN"));
    }

    @Test
    void shouldLogoutSuccessfully() {
        String email = "logouttest@example.com";
        String password = "SecurePass123";

        registerTestUser(email, password);

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "email": "%s",
                            "password": "%s"
                        }
                        """.formatted(email, password))
                .when()
                .post("/api/auth/login");

        String refreshToken = loginResponse.getCookie("refresh_token");

        given()
                .contentType(ContentType.JSON)
                .cookie("refresh_token", refreshToken)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(200)
                .body("data", equalTo("Logged out"));
    }

    private void registerTestUser(String email, String password) {
        String requestBody = """
                {
                    "email": "%s",
                    "password": "%s",
                    "confirmPassword": "%s",
                    "accountType": "COMPANY"
                }
                """.formatted(email, password, password);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201);
    }
}
