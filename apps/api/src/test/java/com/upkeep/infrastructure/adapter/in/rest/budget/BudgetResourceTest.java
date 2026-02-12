package com.upkeep.infrastructure.adapter.in.rest.budget;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
@DisplayName("BudgetResource")
class BudgetResourceTest {

    private String createUserAndGetToken(String email) {
        String registerBody = String.format("""
                {
                    "email": "%s",
                    "password": "SecurePass123",
                    "confirmPassword": "SecurePass123",
                    "accountType": "COMPANY"
                }
                """, email);

        given()
                .contentType(ContentType.JSON)
                .body(registerBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201);

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                            "email": "%s",
                            "password": "SecurePass123"
                        }
                        """, email))
                .when()
                .post("/api/auth/login");

        return loginResponse.getCookie("access_token");
    }

    private String createCompany(String token, String companyName, String companySlug) {
        String createCompanyBody = String.format("""
                {
                    "name": "%s",
                    "slug": "%s"
                }
                """, companyName, companySlug);

        Response response = given()
                .contentType(ContentType.JSON)
                .cookie("access_token", token)
                .body(createCompanyBody)
                .when()
                .post("/api/companies")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String companyId = response.path("data.id");
        if (companyId == null) {
            throw new IllegalStateException("Company creation failed: companyId is null. Response: " + response.asString());
        }
        return companyId;
    }

    @Test
    @DisplayName("should return empty budget summary when no budget set")
    void shouldReturnEmptyBudgetSummary() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String email = "empty-budget-" + uniqueId + "@example.com";
        String token = createUserAndGetToken(email);
        String companyId = createCompany(token, "Empty Budget Company", "empty-budget-" + uniqueId);

        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", token)
                .when()
                .get("/api/companies/" + companyId + "/budget")
                .then()
                .statusCode(200)
                .body("data.exists", equalTo(false))
                .body("data.totalCents", equalTo(0))
                .body("data.allocatedCents", equalTo(0))
                .body("data.remainingCents", equalTo(0))
                .body("data.budgetId", nullValue());
    }

    @Test
    @DisplayName("should set monthly budget successfully")
    void shouldSetBudgetSuccessfully() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String email = "set-budget-" + uniqueId + "@example.com";
        String token = createUserAndGetToken(email);
        String companyId = createCompany(token, "Set Budget Company", "set-budget-" + uniqueId);

        String requestBody = """
                {
                    "amountCents": 50000,
                    "currency": "EUR"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", token)
                .body(requestBody)
                .when()
                .post("/api/companies/" + companyId + "/budget")
                .then()
                .statusCode(201)
                .body("data.budgetId", notNullValue())
                .body("data.amountCents", equalTo(50000))
                .body("data.currency", equalTo("EUR"));
    }

    @Test
    @DisplayName("should return budget summary after setting budget")
    void shouldReturnBudgetSummaryAfterSetting() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String email = "summary-" + uniqueId + "@example.com";
        String token = createUserAndGetToken(email);
        String companyId = createCompany(token, "Summary Company", "summary-" + uniqueId);

        String requestBody = """
                {
                    "amountCents": 100000,
                    "currency": "USD"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", token)
                .body(requestBody)
                .when()
                .post("/api/companies/" + companyId + "/budget")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", token)
                .when()
                .get("/api/companies/" + companyId + "/budget")
                .then()
                .statusCode(200)
                .body("data.exists", equalTo(true))
                .body("data.totalCents", equalTo(100000))
                .body("data.allocatedCents", equalTo(0))
                .body("data.remainingCents", equalTo(100000))
                .body("data.currency", equalTo("USD"))
                .body("data.budgetId", notNullValue());
    }

    @Test
    @DisplayName("should reject budget with amount below minimum")
    void shouldRejectBudgetBelowMinimum() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String email = "below-min-" + uniqueId + "@example.com";
        String token = createUserAndGetToken(email);
        String companyId = createCompany(token, "Below Min Company", "below-min-" + uniqueId);

        String requestBody = """
                {
                    "amountCents": 50,
                    "currency": "EUR"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", token)
                .body(requestBody)
                .when()
                .post("/api/companies/" + companyId + "/budget")
                .then()
                .statusCode(400)
                .body("error.code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("should reject budget with invalid currency")
    void shouldRejectInvalidCurrency() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String email = "invalid-currency-" + uniqueId + "@example.com";
        String token = createUserAndGetToken(email);
        String companyId = createCompany(token, "Invalid Currency Company", "invalid-currency-" + uniqueId);

        String requestBody = """
                {
                    "amountCents": 50000,
                    "currency": "XYZ"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", token)
                .body(requestBody)
                .when()
                .post("/api/companies/" + companyId + "/budget")
                .then()
                .statusCode(400)
                .body("error.code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("should reject budget request without authentication")
    void shouldRejectUnauthenticatedRequest() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String email = "unauth-" + uniqueId + "@example.com";
        String token = createUserAndGetToken(email);
        String companyId = createCompany(token, "Unauth Company", "unauth-" + uniqueId);

        String requestBody = """
                {
                    "amountCents": 50000,
                    "currency": "EUR"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/companies/" + companyId + "/budget")
                .then()
                .statusCode(401)
                .body("error.code", equalTo("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("should set budget with different currencies")
    void shouldSetBudgetWithDifferentCurrencies() {
        String[] currencies = {"EUR", "USD", "GBP"};

        for (String currency : currencies) {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String email = "owner-" + currency.toLowerCase() + "-" + uniqueId + "@example.com";
            String registerBody = String.format("""
                    {
                        "email": "%s",
                        "password": "SecurePass123",
                        "confirmPassword": "SecurePass123",
                        "accountType": "COMPANY"
                    }
                    """, email);

            given()
                    .contentType(ContentType.JSON)
                    .body(registerBody)
                    .post("/api/auth/register")
                    .then()
                    .statusCode(201);

            String loginBody = String.format("""
                    {
                        "email": "%s",
                        "password": "SecurePass123"
                    }
                    """, email);

            Response loginResponse = given()
                    .contentType(ContentType.JSON)
                    .body(loginBody)
                    .post("/api/auth/login");

            String token = loginResponse.getCookie("access_token");

            String createCompanyBody = String.format("""
                    {
                        "name": "Company %s",
                        "slug": "company-%s-%s"
                    }
                    """, currency, currency.toLowerCase(), uniqueId);

            String testCompanyId = given()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", token)
                    .body(createCompanyBody)
                    .post("/api/companies")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("data.id");

            String budgetBody = String.format("""
                    {
                        "amountCents": 75000,
                        "currency": "%s"
                    }
                    """, currency);

            given()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", token)
                    .body(budgetBody)
                    .post("/api/companies/" + testCompanyId + "/budget")
                    .then()
                    .statusCode(201)
                    .body("data.currency", equalTo(currency));
        }
    }

    @Test
    @DisplayName("should reject duplicate budget for same month")
    void shouldRejectDuplicateBudgetForSameMonth() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String email = "duplicate-budget-" + uniqueId + "@example.com";
        String token = createUserAndGetToken(email);
        String companyId = createCompany(token, "Duplicate Budget Company", "duplicate-budget-" + uniqueId);

        String requestBody = """
                {
                    "amountCents": 50000,
                    "currency": "EUR"
                }
                """;

        // First budget should succeed
        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", token)
                .body(requestBody)
                .when()
                .post("/api/companies/" + companyId + "/budget")
                .then()
                .statusCode(201);

        // Second budget for the same month should fail
        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", token)
                .body(requestBody)
                .when()
                .post("/api/companies/" + companyId + "/budget")
                .then()
                .statusCode(409)
                .body("error.code", equalTo("BUDGET_ALREADY_EXISTS"));
    }
}
