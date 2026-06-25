package roomescape.ratelimit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.domain.reservation.ReservationService;

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "rate-limit.capacity=1",
        "rate-limit.refill-per-sec=0.001"
    }
)
class RateLimitIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 초과_요청은_컨트롤러를_호출하지_않고_429로_거부한다() {
        LocalDate date = LocalDate.of(2099, 12, 31);
        given(reservationService.getReservations(date, 1L)).willReturn(List.of());

        RestAssured.given()
            .queryParam("date", date.toString())
            .queryParam("themeId", 1)
            .when().get("/reservations")
            .then().statusCode(200);

        RestAssured.given()
            .queryParam("date", date.toString())
            .queryParam("themeId", 1)
            .when().get("/reservations")
            .then()
            .statusCode(429)
            .header("Retry-After", "1000");

        verify(reservationService, times(1)).getReservations(date, 1L);
    }
}
