package roomescape.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = {"rate-limit.capacity=2", "rate-limit.refill-per-sec=0.1"})
class RateLimitIntegrationTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("한도 내 요청은 통과하고 초과 요청은 429와 Retry-After로 거부된다.")
    void rejectOverConfiguredLimit() {
        RestAssured.given().queryParam("userName", "브라운").when().get("/reservations").then().statusCode(200);
        RestAssured.given().queryParam("userName", "브라운").when().get("/reservations").then().statusCode(200);

        RestAssured.given()
                .queryParam("userName", "브라운")
                .when().get("/reservations")
                .then()
                .statusCode(429)
                .header("Retry-After", notNullValue());
    }
}
