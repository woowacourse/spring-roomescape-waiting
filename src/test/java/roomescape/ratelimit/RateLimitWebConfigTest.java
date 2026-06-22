package roomescape.ratelimit;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.FixedClockConfig;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "rate-limit.capacity=1",
                "rate-limit.refill-per-second=0.1"
        }
)
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RateLimitWebConfigTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("예약 생성은 한도를 넘으면 429로 거부하고, 예약 조회는 같은 한도로 막지 않는다.")
    void limitsReservationCreationWithoutLimitingReservationQuery() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationRequest("limited-user-1"))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationRequest("limited-user-2"))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(429)
                .header("Retry-After", is("10"));

        RestAssured.given().log().all()
                .queryParam("name", "user_a")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200);
    }

    private Map<String, Object> reservationRequest(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 2L);
        return params;
    }
}
