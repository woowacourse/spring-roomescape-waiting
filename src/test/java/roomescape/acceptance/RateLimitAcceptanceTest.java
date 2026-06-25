package roomescape.acceptance;

import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
        "rate-limit.capacity=1",
        "rate-limit.refill-per-sec=0.0001"
})
@Sql(value = "/empty.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class RateLimitAcceptanceTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 한도를_초과한_요청은_컨트롤러_호출_없이_429와_Retry_After로_거부된다() {
        // 한도(capacity=1) 내 첫 요청은 정상 처리
        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().get("/api/reservations")
                .then().log().all()
                .statusCode(200);

        // 초과 요청은 429 + Retry-After 로 거부
        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().get("/api/reservations")
                .then().log().all()
                .statusCode(429)
                .header("Retry-After", notNullValue());
    }
}
