package roomescape.domain.reservationtime;

import static io.restassured.RestAssured.given;
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
class ReservationTimeIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_slot");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM reservation_date");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    @DisplayName("예약 가능 시간 조회를 end-to-end로 확인한다.")
    void getReservationTimeAvailability() {
        jdbcTemplate.update(
            "INSERT INTO theme(name, content, url) VALUES (?, ?, ?)",
            "공포", "무서운 테마", "theme-url"
        );
        jdbcTemplate.update(
            "INSERT INTO reservation_date(date) VALUES (?)",
            "2026-06-01"
        );
        jdbcTemplate.update(
            "INSERT INTO reservation_time(start_at) VALUES (?)",
            "10:00"
        );
        jdbcTemplate.update(
            "INSERT INTO reservation_time(start_at) VALUES (?)",
            "11:00"
        );
        Long themeId = jdbcTemplate.queryForObject(
            "SELECT id FROM theme WHERE name = ?",
            Long.class,
            "공포"
        );
        Long dateId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_date WHERE date = ?",
            Long.class,
            "2026-06-01"
        );
        Long firstTimeId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_time WHERE start_at = ?",
            Long.class,
            "10:00:00"
        );
        Long secondTimeId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_time WHERE start_at = ?",
            Long.class,
            "11:00:00"
        );
        jdbcTemplate.update(
            "INSERT INTO reservation_slot(date_id, time_id, theme_id) VALUES (?, ?, ?)",
            dateId,
            firstTimeId,
            themeId
        );

        given().log().all()
            .contentType(ContentType.JSON)
            .param("themeId", themeId)
            .param("dateId", dateId)
            .when().get("/reservation-times/availability")
            .then().log().all()
            .statusCode(200)
            .body("[0].timeId", is(firstTimeId.intValue()))
            .body("[0].startAt", is("10:00"))
            .body("[0].available", is(false))
            .body("[1].timeId", is(secondTimeId.intValue()))
            .body("[1].startAt", is("11:00"))
            .body("[1].available", is(true));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 시 themeId 파라미터가 누락되었을 경우 400 에러가 발생한다.")
    void getReservationTimeAvailabilityWithoutThemeId() {
        given().log().all()
            .contentType(ContentType.JSON)
            .param("dateId", 1L)
            .when().get("/reservation-times/availability")
            .then().log().all()
            .statusCode(400)
            .body("code", is("REQUIRED_PARAMETER_MISSING"))
            .body("message", is("필수 요청 파라미터가 누락되었습니다."));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 시 존재하지 않는 테마일 경우 404 에러가 발생한다.")
    void getReservationTimeAvailabilityWhenThemeNotFound() {
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
            .param("themeId", 999L)
            .param("dateId", dateId)
            .when().get("/reservation-times/availability")
            .then().log().all()
            .statusCode(404)
            .body("code", is("THEME_NOT_EXIST"))
            .body("message", is("존재하지 않는 테마 입니다."));
    }
}
