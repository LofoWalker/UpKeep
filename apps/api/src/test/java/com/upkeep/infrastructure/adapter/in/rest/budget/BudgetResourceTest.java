package com.upkeep.infrastructure.adapter.in.rest.budget;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
@DisplayName("BudgetResource")
class BudgetResourceTest {

    private String accessToken;
    private String companyId;

    @BeforeEach
    void setUp() {
        String registerBody = """
                {
                    "email": "budgetowner@example.com",
                    "password": "SecurePass123",
                    "confirmPassword": "SecurePass123",
                    "accountType": "COMPANY"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(registerBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201);

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "email": "budgetowner@example.com",
                            "password": "SecurePass123"
                        }
                        """)
                .when()
                .post("/api/auth/login");

        accessToken = loginResponse.getCookie("access_token");

        String createCompanyBody = """
                {
                    "name": "Budget Test Company",
                    "slug": "budget-test-company"
                }
                """;

        companyId = given()
                .contentType(ContentType.JSON)
                .cookie("access_token", accessToken)
                .body(createCompanyBody)
                .when()
                .post("/api/companies")
                .then()
                .statusCode(201)
                .extract()
                .path("data.companyId");
    }

    @Test
    @DisplayName("should return empty budget summary when no budget set")
    void shouldReturnEmptyBudgetSummary() {
        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", accessToken)
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
        String requestBody = """
                {
                    "amountCents": 50000,
                    "currency": "EUR"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", accessToken)
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
        String requestBody = """
                {
                    "amountCents": 100000,
                    "currency": "USD"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", accessToken)
                .body(requestBody)
                .when()
                .post("/api/companies/" + companyId + "/budget")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", accessToken)
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
        String requestBody = """
                {
                    "amountCents": 50,
                    "currency": "EUR"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", accessToken)
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
        String requestBody = """
                {
                    "amountCents": 50000,
                    "currency": "XYZ"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .cookie("access_token", accessToken)
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
            String email = "owner-" + currency.toLowerCase() + "@example.com";
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
                    .post("/api/auth/register");

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
                        "slug": "company-%s"
                    }
                    """, currency, currency.toLowerCase());

            String testCompanyId = given()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", token)
                    .body(createCompanyBody)
                    .post("/api/companies")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("data.companyId");

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
}
