package roomescape.domain.reservationdate;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql("/truncate.sql")
class ReservationDateControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("사용자 권한으로 가능한 모든 예약 날짜를 조회한다.")
    void getAllReservationDates() {
        String farFutureDate = LocalDate.now().plusYears(10).toString();
        jdbcTemplate.update("insert into reservation_date(play_day) values (?)", farFutureDate);

        RestAssured.given().log().all()
            .when().get("/reservation-dates")
            .then().log().all()
            .statusCode(200)
            .body("any { it.playDay == '" + farFutureDate + "' }", is(true));
    }
}
