package roomescape.domain.reservationtime;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")
class ReservationTimeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("사용자 권한으로 특정 날짜/테마의 예약 가능 여부를 포함한 시간을 조회한다.")
    void getReservationTimeAvailability() {
        reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(23, 0)));

        RestAssured.given().log().all()
                .param("themeId", 1L)
                .param("dateId", 1L)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("any { it.startAt == '23:00' }", is(true));
    }
}
