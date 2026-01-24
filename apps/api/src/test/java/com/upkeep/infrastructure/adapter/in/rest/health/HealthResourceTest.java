package com.upkeep.infrastructure.adapter.in.rest.health;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class HealthResourceTest {

    @Test
    void testHealthEndpoint() {
        given()
                .when().get("/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("data.status", equalTo("UP"))
                .body("data.message", equalTo("Upkeep API is running"))
                .body("meta.timestamp", notNullValue())
                .body("meta.traceId", notNullValue())
                .body("error", nullValue());
    }
}
