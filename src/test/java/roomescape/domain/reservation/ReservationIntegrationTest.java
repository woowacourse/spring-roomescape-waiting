package roomescape.domain.reservation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("DELETE FROM user_reservation");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM reservation_date");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    @DisplayName("예약 생성을 end-to-end로 확인한다.")
    void createReservation() {
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");
        Long timeId = saveTime("10:00");

        String request = """
            {
                "name": "보예",
                "dateId": %d,
                "timeId": %d,
                "themeId": %d
            }
            """.formatted(dateId, timeId, themeId);

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("date", is("2026-06-01"))
            .body("time", is("10:00"))
            .body("theme.name", is("공포"))
            .body("theme.content", is("무서운 테마"))
            .body("theme.url", is("theme-url"));

        given()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then()
            .statusCode(200)
            .body("name", is("보예"))
            .body("reservation", hasSize(1));
    }

    @Test
    @DisplayName("예약 생성 시 시간 필드가 누락되었을 경우 400 에러가 발생한다.")
    void createReservationWithoutTimeId() {
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");

        String request = """
            {
                "name": "보예",
                "dateId": %d,
                "themeId": %d
            }
            """.formatted(dateId, themeId);

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400)
            .body("code", is("INPUT_VALIDATION_ERROR"))
            .body("message", is("시간은 필수 선택 사항 입니다. 시간을 선택해주세요."));
    }

    @Test
    @DisplayName("예약자 이름으로 예약 조회를 end-to-end로 확인한다.")
    void getUserReservations() {
        saveReservation("보예", "2026-06-01", "10:00", "공포");

        given().log().all()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("name", is("보예"))
            .body("reservation[0].date.startWhen", is("2026-06-01"))
            .body("reservation[0].time.startAt", is("10:00"))
            .body("reservation[0].theme.name", is("공포"));
    }

    @Test
    @DisplayName("예약 조회 시 이름 파라미터가 누락되었을 경우 400 에러가 발생한다.")
    void getUserReservationsWithoutName() {
        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/reservations")
            .then().log().all()
            .statusCode(400)
            .body("code", is("REQUIRED_PARAMETER_MISSING"))
            .body("message", is("필수 요청 파라미터가 누락되었습니다."));
    }

    @Test
    @DisplayName("예약 삭제를 end-to-end로 확인한다.")
    void deleteUserReservation() {
        Long reservationId = saveReservation("보예", "2026-06-01", "10:00", "공포");

        given().log().all()
            .contentType(ContentType.JSON)
            .when().delete("/reservations/{id}", reservationId)
            .then().log().all()
            .statusCode(204);

        given()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then()
            .statusCode(200)
            .body("name", is("보예"))
            .body("reservation", hasSize(0));
    }

    @Test
    @DisplayName("예약 수정을 end-to-end로 확인한다.")
    void updateReservation() {
        Long reservationId = saveReservation("보예", "2026-06-01", "10:00", "공포");
        saveDate("2026-06-02");
        saveTime("11:00");

        String request = """
            {
                "startWhen": "2026-06-02",
                "startAt": "11:00"
            }
            """;

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().patch("/reservations/{id}", reservationId)
            .then().log().all()
            .statusCode(204);

        given()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then()
            .statusCode(200)
            .body("reservation[0].date.startWhen", is("2026-06-02"))
            .body("reservation[0].time.startAt", is("11:00"));
    }

    private Long saveReservation(String name, String date, String time, String themeName) {
        Long themeId = saveTheme(themeName);
        Long dateId = saveDate(date);
        Long timeId = saveTime(time);

        jdbcTemplate.update("INSERT INTO users(name) VALUES (?)", name);
        Long userId = jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE name = ?",
            Long.class,
            name
        );
        jdbcTemplate.update(
            "INSERT INTO reservation(date_id, time_id, theme_id) VALUES (?, ?, ?)",
            dateId,
            timeId,
            themeId
        );
        Long reservationId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation WHERE date_id = ? AND time_id = ? AND theme_id = ?",
            Long.class,
            dateId,
            timeId,
            themeId
        );
        jdbcTemplate.update(
            "INSERT INTO user_reservation(user_id, reservation_id, waiting_number, status) VALUES (?, ?, ?, ?)",
            userId,
            reservationId,
            null,
            "CONFIRMED"
        );

        return reservationId;
    }

    private Long saveTheme(String themeName) {
        jdbcTemplate.update(
            "INSERT INTO theme(name, content, url) VALUES (?, ?, ?)",
            themeName,
            "무서운 테마",
            "theme-url"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM theme WHERE name = ?",
            Long.class,
            themeName
        );
    }

    private Long saveDate(String date) {
        jdbcTemplate.update(
            "INSERT INTO reservation_date(date) VALUES (?)",
            date
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_date WHERE date = ?",
            Long.class,
            date
        );
    }

    private Long saveTime(String time) {
        jdbcTemplate.update(
            "INSERT INTO reservation_time(start_at) VALUES (?)",
            time
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_time WHERE start_at = ?",
            Long.class,
            time + ":00"
        );
    }
}
