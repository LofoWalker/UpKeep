package com.upkeep.infrastructure.adapter.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
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
            .body("error.code", equalTo("CONFLICT"))
            .body("error.message", containsString("already exists"));
    }
}
