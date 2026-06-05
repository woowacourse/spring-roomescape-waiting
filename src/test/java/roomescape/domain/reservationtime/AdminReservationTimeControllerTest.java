package roomescape.domain.reservationtime;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
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
class AdminReservationTimeControllerTest {

    private static final String ADMIN_HEADER = "X-ADMIN-TOKEN";

    @LocalServerPort
    private int port;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Value("${token}")
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("관리자 권한으로 예약 시간을 생성한다.")
    void createReservationTime() {
        Map<String, Object> params = new HashMap<>();
        params.put("startAt", "23:00");

        RestAssured.given().log().all()
                .header(ADMIN_HEADER, adminToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", is("23:00"));
    }

    @Test
    @DisplayName("관리자 권한으로 모든 예약 시간을 조회한다.")
    void getAllReservationTimes() {
        reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(23, 0)));

        RestAssured.given().log().all()
                .header(ADMIN_HEADER, adminToken)
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("any { it.startAt == '23:00' }", is(true));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 시간을 삭제한다.")
    void deleteReservationTime() {
        ReservationTime saved = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(23, 30)));

        RestAssured.given().log().all()
                .header(ADMIN_HEADER, adminToken)
                .when().delete("/admin/times/" + saved.getId())
                .then().log().all()
                .statusCode(204);
    }
}
