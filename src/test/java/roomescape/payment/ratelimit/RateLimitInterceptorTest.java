package roomescape.payment.ratelimit;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "rate-limit.capacity=1",
                "rate-limit.refill-per-second=0.001"
        }
)
class RateLimitInterceptorTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 한도_내_요청은_통과하고_초과_요청은_429와_RetryAfter_헤더를_반환한다() {
        RestAssured.given()
                .when().get("/reservations?name=test")
                .then().statusCode(200);

        RestAssured.given()
                .when().get("/reservations?name=test")
                .then()
                .statusCode(429)
                .header(HttpHeaders.RETRY_AFTER, notNullValue());
    }

    @Test
    void Retry_After_헤더_값은_양수이다() {
        RestAssured.given().when().get("/reservations?name=test");

        String retryAfter = RestAssured.given()
                .when().get("/reservations?name=test")
                .then()
                .statusCode(429)
                .extract().header(HttpHeaders.RETRY_AFTER);

        long seconds = Long.parseLong(retryAfter);
        org.assertj.core.api.Assertions.assertThat(seconds).isGreaterThan(0);
    }
}
