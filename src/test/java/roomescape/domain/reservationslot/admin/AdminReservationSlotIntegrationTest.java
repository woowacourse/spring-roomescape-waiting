package roomescape.domain.reservationslot.admin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AdminReservationSlotIntegrationTest {

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
        jdbcTemplate.update("DELETE FROM reservation_slot");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM reservation_date");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    @DisplayName("관리자의 예약 전체 조회를 end-to-end로 확인한다.")
    void getAllReservation() {
        saveThemeDateTimeAndReservation("보예");

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(200)
            .body("[0].date", is("2026-06-01"))
            .body("[0].time.startAt", is("10:00"))
            .body("[0].theme.name", is("공포"));
    }

    @Test
    @DisplayName("관리자가 토큰을 누락했을 경우 401 예외가 발생한다.")
    void getAllReservationWithoutToken() {
        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/admin/reservations")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    @DisplayName("관리자의 예약 취소를 end-to-end로 확인한다.")
    void deleteReservation() {
        Long reservationId = saveThemeDateTimeAndReservation("보예");

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().delete("/admin/reservations/{id}", reservationId)
            .then().log().all()
            .statusCode(204);

        String reservationStatus = jdbcTemplate.queryForObject(
            "SELECT status FROM reservation WHERE id = ?",
            String.class,
            reservationId
        );
        assertThat(reservationStatus).isEqualTo("CANCELED");
    }

    private Long saveThemeDateTimeAndReservation(String name) {
        jdbcTemplate.update(
            "INSERT INTO theme(name, content, url) VALUES (?, ?, ?)",
            "공포",
            "무서운 테마",
            "theme-url"
        );
        jdbcTemplate.update(
            "INSERT INTO reservation_date(date) VALUES (?)",
            "2026-06-01"
        );
        jdbcTemplate.update(
            "INSERT INTO reservation_time(start_at) VALUES (?)",
            "10:00"
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
        Long timeId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_time WHERE start_at = ?",
            Long.class,
            "10:00:00"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation_slot(date_id, time_id, theme_id) VALUES (?, ?, ?)",
                dateId,
                timeId,
                themeId
        );
        Long reservationId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_slot WHERE date_id = ? AND time_id = ? AND theme_id = ?",
            Long.class,
            dateId,
            timeId,
            themeId
        );

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
            reservationId,
            "CONFIRMED"
        );

        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation WHERE user_id = ? AND reservation_slot_id = ?",
            Long.class,
            userId,
            reservationId
        );
    }
}
