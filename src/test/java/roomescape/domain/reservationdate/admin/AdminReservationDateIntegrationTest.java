package roomescape.domain.reservationdate.admin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.TestFixture;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AdminReservationDateIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestFixture testFixture;

    @Value("${token}")
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        testFixture.clear();
    }

    @Test
    @DisplayName("관리자의 예약 날짜 전체 조회를 end-to-end로 확인한다.")
    void getAllReservationDateForAdmin() {
        testFixture.saveDate("2026-06-01");

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/reservation-dates")
            .then().log().all()
            .statusCode(200)
            .body("[0].reservationDate", is("2026-06-01"));
    }

    @Test
    @DisplayName("관리자가 토큰을 누락했을 경우 401 예외가 발생한다.")
    void getAllReservationDateForAdminWithoutToken() {
        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/admin/reservation-dates")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    @DisplayName("관리자의 예약 날짜 생성을 end-to-end로 확인한다.")
    void createReservationDate() {
        String request = """
            {
                "reservationDate": "2026-06-01"
            }
            """;

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .body(request)
            .when().post("/admin/reservation-dates")
            .then().log().all()
            .statusCode(201)
            .body("reservationDate", is("2026-06-01"));

        given()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/reservation-dates")
            .then()
            .statusCode(200)
            .body("reservationDate", hasItem("2026-06-01"));
    }

    @Test
    @DisplayName("관리자 예약 날짜 생성 시 날짜 필드가 누락되었을 경우 400 에러가 발생한다.")
    void createReservationDateWithoutDate() {
        String request = """
            {
            }
            """;

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .body(request)
            .when().post("/admin/reservation-dates")
            .then().log().all()
            .statusCode(400)
            .body("code", is("INPUT_VALIDATION_ERROR"))
            .body("message", is("예약 날짜는 필수 사항 입니다. 날짜를 선택해주세요."));
    }

    @Test
    @DisplayName("관리자 예약 날짜 생성 시 관리자 인증 토큰이 누락되었을 경우 401 에러가 발생한다.")
    void createReservationDateWithoutToken() {
        String request = """
            {
                "reservationDate": "2026-06-01"
            }
            """;

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/reservation-dates")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    @DisplayName("관리자의 예약 날짜 삭제를 end-to-end로 확인한다.")
    void deleteReservationDate() {
        ReservationDate reservationDate = testFixture.saveDate("2026-06-01");

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().delete("/admin/reservation-dates/{id}", reservationDate.getId())
            .then().log().all()
            .statusCode(204);

        given()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/reservation-dates")
            .then()
            .statusCode(200)
            .body("reservationDate", not(hasItem("2026-06-01")));
    }

    @Test
    @DisplayName("이미 예약이 존재하는 날짜는 삭제할 수 없다.")
    void deleteReservationDateWhenDateInUse() {
        ReservationDate reservationDate = testFixture.saveDate("2026-06-01");
        ReservationTime reservationTime = testFixture.saveTime("10:00");
        Theme theme = testFixture.saveTheme("공포");
        ReservationSlot reservationSlot = testFixture.saveSlot(reservationDate, reservationTime, theme);

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().delete("/admin/reservation-dates/{id}", reservationSlot.getDate().getId())
            .then().log().all()
            .statusCode(409)
            .body("code", is("RESERVATION_DATE_IN_USE"))
            .body("message", is("이미 예약이 존재하는 날짜는 삭제할 수 없습니다."));
    }
}
