package roomescape.ratelimit;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.reservation.service.ReservationService;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:rate-limit-test",
                "rate-limit.capacity=1",
                "rate-limit.refill-per-second=0",
                "outbound-rate-limit.capacity=100",
                "outbound-rate-limit.refill-per-second=100",
                "outbound-rate-limit.max-attempts=3",
                "outbound-rate-limit.fallback-delay=1s"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RateLimitControllerTest {

    @LocalServerPort
    private int port;

    @MockitoSpyBean
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 한도_초과_예약_요청은_컨트롤러_호출_없이_429로_거부된다() {
        RestAssured.given()
                .when().get("/reservations")
                .then()
                .statusCode(200);

        RestAssured.given()
                .when().get("/reservations")
                .then()
                .statusCode(429)
                .header(HttpHeaders.RETRY_AFTER, is("1"));

        verify(reservationService, times(1)).findAll();
    }
}
