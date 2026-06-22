package roomescape.global.ratelimit;

import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import roomescape.testSupport.SpringWebTest;

@SpringWebTest
@TestPropertySource(properties = {
        "rate-limit.capacity=1",
        "rate-limit.refill-per-second=0.001"
})
class RateLimitInterceptorTest {

    @Test
    void 한도를_초과하면_컨트롤러_호출없이_429와_RetryAfter를_반환한다() {
        // 1차: 토큰 1개 소비, 컨트롤러까지 도달해 파라미터 누락으로 400.
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(400);

        // 2차: 토큰이 없어 컨트롤러 호출 전에 차단(다시 400이 아니라 429).
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(429)
                .header("Retry-After", notNullValue());
    }
}
