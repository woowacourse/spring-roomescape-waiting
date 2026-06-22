package roomescape.ratelimit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "toss.client-key=test_gck_test",
                "toss.secret-key=test_gsk_test",
                "rate-limit.capacity=1",
                "rate-limit.refill-per-second=0.1",
                "outbound-rate-limit.capacity=1000",
                "outbound-rate-limit.refill-per-second=1000.0"
        }
)
class RateLimitIntegrationTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 설정된_한도를_넘으면_컨트롤러_처리_전에_429로_거부한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/reservations")
                .then().log().all()
                .statusCode(429)
                .header("Retry-After", notNullValue())
                .body(equalTo(""));
    }
}
