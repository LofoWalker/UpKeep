package com.upkeep.infrastructure.adapter.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test for health check endpoint.
 * Verifies the API is running correctly.
 */
@QuarkusTest
class HealthControllerTest {

    @Test
    void testHealthEndpoint() {
        given()
            .when()
            .get("/health")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("UP"))
            .body("message", equalTo("Upkeep API is running"));
    }
}

