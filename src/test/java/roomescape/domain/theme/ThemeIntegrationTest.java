package roomescape.domain.theme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ThemeIntegrationTest {

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
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    @DisplayName("전체 테마 조회를 end-to-end로 확인한다.")
    void getAllTheme() {
        jdbcTemplate.update(
            "INSERT INTO theme(name, content, url) VALUES (?, ?, ?)",
            "공포", "무서운 테마", "theme-url"
        );
        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/themes")
            .then().log().all()
            .statusCode(200)
            .body("[0].name", is("공포"))
            .body("[0].content", is("무서운 테마"))
            .body("[0].url", is("theme-url"));
    }

    @Test
    @DisplayName("인기 테마 조회는 예약 슬롯 id와 예약 id가 달라도 실제 예약 슬롯 기준으로 집계한다.")
    void getThemeRankByReservationSlotId() {
        LocalDate reservationDate = LocalDate.now().minusDays(1);
        jdbcTemplate.update(
            "INSERT INTO theme(name, content, url) VALUES (?, ?, ?)",
            "공포", "무서운 테마", "theme-url"
        );
        jdbcTemplate.update(
            "INSERT INTO theme(name, content, url) VALUES (?, ?, ?)",
            "보예", "보예 테마", "boye-url"
        );
        jdbcTemplate.update(
            "INSERT INTO reservation_date(date) VALUES (?)",
            reservationDate
        );
        jdbcTemplate.update(
            "INSERT INTO reservation_time(start_at) VALUES (?)",
            "10:00"
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, "공포");
        Long dummyThemeId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, "보예");
        Long dateId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_date WHERE date = ?",
            Long.class,
            reservationDate
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
            dummyThemeId
        );
        jdbcTemplate.update(
            "INSERT INTO reservation_slot(date_id, time_id, theme_id) VALUES (?, ?, ?)",
            dateId,
            timeId,
            themeId
        );
        Long targetSlotId = jdbcTemplate.queryForObject(
            "SELECT MAX(id) FROM reservation_slot",
            Long.class
        );
        jdbcTemplate.update("INSERT INTO users(name) VALUES (?)", "보예");
        Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE name = ?", Long.class, "보예");
        jdbcTemplate.update(
            "INSERT INTO reservation(user_id, reservation_slot_id, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            userId,
            targetSlotId,
            "CONFIRMED"
        );

        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/themes/rank")
            .then().log().all()
            .statusCode(200)
            .body("[0].themeName", is("공포"));
    }
}
