package com.upkeep.infrastructure.adapter.in.rest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
@QuarkusTest
class ApiDemoControllerTest {
    @Test
    void testSuccessResponse() {
        given()
            .when().get("/api/demo/success")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("data.id", equalTo("demo-123"))
            .body("data.message", equalTo("Success response"))
            .body("meta.timestamp", notNullValue())
            .body("meta.traceId", notNullValue())
            .body("error", nullValue());
    }
    @Test
    void testValidationError() {
        given()
            .when().get("/api/demo/validation-error")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("VALIDATION_ERROR"))
            .body("error.message", equalTo("Invalid input provided"))
            .body("error.details", hasSize(2))
            .body("error.details[0].field", equalTo("email"))
            .body("error.details[0].message", equalTo("Invalid email format"))
            .body("error.details[1].field", equalTo("age"))
            .body("error.traceId", notNullValue())
            .body("data", nullValue())
            .body("meta", nullValue());
    }
    @Test
    void testUnauthorized() {
        given()
            .when().get("/api/demo/unauthorized")
            .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("UNAUTHORIZED"))
            .body("error.message", equalTo("Authentication required"))
            .body("error.traceId", notNullValue());
    }
    @Test
    void testForbidden() {
        given()
            .when().get("/api/demo/forbidden")
            .then()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("FORBIDDEN"))
            .body("error.message", containsString("permission"));
    }
    @Test
    void testNotFound() {
        given()
            .when().get("/api/demo/not-found/abc-123")
            .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("NOT_FOUND"))
            .body("error.message", containsString("User not found: abc-123"));
    }
    @Test
    void testConflict() {
        given()
            .when().get("/api/demo/conflict")
            .then()
            .statusCode(409)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("CONFLICT"))
            .body("error.message", containsString("already exists"));
    }
    @Test
    void testDomainRule() {
        given()
            .when().get("/api/demo/domain-rule")
            .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("DOMAIN_RULE_VIOLATION"))
            .body("error.message", containsString("active subscriptions"));
    }
    @Test
    void testInternalError() {
        given()
            .when().get("/api/demo/internal-error")
            .then()
            .statusCode(500)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("INTERNAL_ERROR"))
            .body("error.message", equalTo("An unexpected error occurred"))
            .body("error.traceId", notNullValue());
    }
    @Test
    void testAllStatusesEndpoint() {
        given()
            .when().get("/api/demo/all-statuses")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("data.200", notNullValue())
            .body("data.400", notNullValue())
            .body("data.401", notNullValue())
            .body("data.403", notNullValue())
            .body("data.404", notNullValue())
            .body("data.409", notNullValue())
            .body("data.422", notNullValue())
            .body("data.500", notNullValue())
            .body("meta.timestamp", notNullValue())
            .body("meta.traceId", notNullValue());
    }
}
