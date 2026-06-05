package roomescape.domain.reservationdate;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")
class AdminReservationDateControllerTest {

    private static final String ADMIN_HEADER = "X-ADMIN-TOKEN";

    @LocalServerPort
    private int port;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Value("${token}")
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("관리자 권한으로 예약 날짜를 생성한다.")
    void createReservationDate() {
        String farFutureDate = LocalDate.now().plusYears(10).toString();
        Map<String, Object> params = new HashMap<>();
        params.put("playDay", farFutureDate);

        RestAssured.given().log().all()
                .header(ADMIN_HEADER, adminToken)
                .contentType("application/json")
                .body(params)
                .when().post("/admin/reservation-dates")
                .then().log().all()
                .statusCode(201)
                .body("playDay", is(farFutureDate));
    }

    @Test
    @DisplayName("관리자 권한으로 모든 예약 날짜를 조회한다.")
    void getAllReservationDateForAdmin() {
        LocalDate farFutureDate = LocalDate.now().plusYears(10);
        reservationDateRepository.save(ReservationDate.createWithoutId(farFutureDate));

        RestAssured.given().log().all()
                .header(ADMIN_HEADER, adminToken)
                .when().get("/admin/reservation-dates")
                .then().log().all()
                .statusCode(200)
                .body("any { it.playDay == '" + farFutureDate + "' }", is(true));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 날짜를 삭제한다.")
    void deleteReservationDate() {
        ReservationDate saved = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusYears(10)));

        RestAssured.given().log().all()
                .header(ADMIN_HEADER, adminToken)
                .when().delete("/admin/reservation-dates/" + saved.getId())
                .then().log().all()
                .statusCode(204);
    }
}
