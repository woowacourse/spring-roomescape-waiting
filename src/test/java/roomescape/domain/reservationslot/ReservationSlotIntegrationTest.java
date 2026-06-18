package roomescape.domain.reservationslot;

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
class ReservationSlotIntegrationTest {

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
    @DisplayName("예약 슬롯 조회를 end-to-end로 확인한다.")
    void getReservationSlots() {
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");
        Long firstTimeId = saveTime("10:00");
        Long secondTimeId = saveTime("11:00");
        Long reservationSlotId = saveReservationSlot(dateId, firstTimeId, themeId);
        saveReservation("보예", reservationSlotId, "CONFIRMED");

        given().log().all()
            .contentType(ContentType.JSON)
            .param("themeId", themeId)
            .param("dateId", dateId)
            .when().get("/reservation-slots")
            .then().log().all()
            .statusCode(200)
            .body("[0].timeId", is(firstTimeId.intValue()))
            .body("[0].startAt", is("10:00"))
            .body("[0].waitingNumber", is(1))
            .body("[1].timeId", is(secondTimeId.intValue()))
            .body("[1].startAt", is("11:00"))
            .body("[1].waitingNumber", is(0));
    }

    @Test
    @DisplayName("예약 슬롯만 있고 실제 예약이 없으면 예약 인원은 0명으로 조회된다.")
    void getReservationSlotsWhenReservationSlotHasNoReservation() {
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");
        Long timeId = saveTime("10:00");
        saveReservationSlot(dateId, timeId, themeId);

        given().log().all()
            .contentType(ContentType.JSON)
            .param("themeId", themeId)
            .param("dateId", dateId)
            .when().get("/reservation-slots")
            .then().log().all()
            .statusCode(200)
            .body("[0].timeId", is(timeId.intValue()))
            .body("[0].startAt", is("10:00"))
            .body("[0].waitingNumber", is(0));
    }

    @Test
    @DisplayName("예약 슬롯 조회 시 themeId 파라미터가 누락되었을 경우 400 에러가 발생한다.")
    void getReservationSlotsWithoutThemeId() {
        given().log().all()
            .contentType(ContentType.JSON)
            .param("dateId", 1L)
            .when().get("/reservation-slots")
            .then().log().all()
            .statusCode(400)
            .body("code", is("REQUIRED_PARAMETER_MISSING"))
            .body("message", is("필수 요청 파라미터가 누락되었습니다."));
    }

    @Test
    @DisplayName("예약 슬롯 조회 시 존재하지 않는 테마일 경우 404 에러가 발생한다.")
    void getReservationSlotsWhenThemeNotFound() {
        Long dateId = saveDate("2026-06-01");

        given().log().all()
            .contentType(ContentType.JSON)
            .param("themeId", 999L)
            .param("dateId", dateId)
            .when().get("/reservation-slots")
            .then().log().all()
            .statusCode(404)
            .body("code", is("THEME_NOT_EXIST"))
            .body("message", is("존재하지 않는 테마 입니다."));
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
        jdbcTemplate.update("INSERT INTO reservation_date(date) VALUES (?)", date);
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_date WHERE date = ?",
            Long.class,
            date
        );
    }

    private Long saveTime(String time) {
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", time);
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_time WHERE start_at = ?",
            Long.class,
            time + ":00"
        );
    }

    private Long saveReservationSlot(Long dateId, Long timeId, Long themeId) {
        jdbcTemplate.update(
            "INSERT INTO reservation_slot(date_id, time_id, theme_id) VALUES (?, ?, ?)",
            dateId,
            timeId,
            themeId
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_slot WHERE date_id = ? AND time_id = ? AND theme_id = ?",
            Long.class,
            dateId,
            timeId,
            themeId
        );
    }

    private Long saveReservation(String name, Long reservationSlotId, String status) {
        jdbcTemplate.update("INSERT INTO users(name) VALUES (?)", name);
        Long userId = jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE name = ?",
            Long.class,
            name
        );
        jdbcTemplate.update(
            "INSERT INTO reservation(user_id, reservation_slot_id, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            userId,
            reservationSlotId,
            status
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation WHERE user_id = ? AND reservation_slot_id = ?",
            Long.class,
            userId,
            reservationSlotId
        );
    }
}
