package roomescape.acceptance;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "rate-limit.capacity=3.0",
        "rate-limit.refill-per-sec=0.1"
})
class RateLimitAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("한도 초과 요청 시 429 응답과 Retry-After 헤더를 반환한다.")
    void rateLimitExceeded() {
        // 3개까지는 통과 (capacity=3)
        for (int i = 0; i < 3; i++) {
            RestAssured.given().log().all()
                    .when().get("/reservations/mine")
                    .then().log().all()
                    .statusCode(401); // 인증은 안 했으므로 401이지만 RateLimit은 통과함
        }

        // 4번째 요청은 한도 초과로 429
        RestAssured.given().log().all()
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(429)
                .header("Retry-After", notNullValue());
    }
}
