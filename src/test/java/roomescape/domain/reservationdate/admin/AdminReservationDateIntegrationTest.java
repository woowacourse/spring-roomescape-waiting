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
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AdminReservationDateIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${token}")
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_date");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    @DisplayName("관리자의 예약 날짜 전체 조회를 end-to-end로 확인한다.")
    void getAllReservationDateForAdmin() {
        jdbcTemplate.update(
            "INSERT INTO reservation_date(date) VALUES (?)",
            "2026-06-01"
        );

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
        jdbcTemplate.update(
            "INSERT INTO reservation_date(date) VALUES (?)",
            "2026-06-01"
        );

        Long dateId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_date WHERE date = ?",
            Long.class,
            "2026-06-01"
        );

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().delete("/admin/reservation-dates/{id}", dateId)
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
        jdbcTemplate.update(
            "INSERT INTO reservation_date(date) VALUES (?)",
            "2026-06-01"
        );
        jdbcTemplate.update(
            "INSERT INTO reservation_time(start_at) VALUES (?)",
            "10:00"
        );
        jdbcTemplate.update(
            "INSERT INTO theme(name, content, url) VALUES (?, ?, ?)",
            "공포",
            "무서운 테마",
            "theme-url"
        );

        Long dateId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_date WHERE date = ?",
            Long.class,
            "2026-06-01"
        );
        Long timeId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_time WHERE start_at = ?",
            Long.class,
            "10:00:00"
        );
        Long themeId = jdbcTemplate.queryForObject(
            "SELECT id FROM theme WHERE name = ?",
            Long.class,
            "공포"
        );

        jdbcTemplate.update(
            "INSERT INTO reservation(name, date_id, time_id, theme_id) VALUES (?, ?, ?, ?)",
            "보예",
            dateId,
            timeId,
            themeId
        );

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().delete("/admin/reservation-dates/{id}", dateId)
            .then().log().all()
            .statusCode(409)
            .body("code", is("RESERVATION_DATE_IN_USE"))
            .body("message", is("이미 예약이 존재하는 날짜는 삭제할 수 없습니다."));
    }
}
