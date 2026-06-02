package roomescape.domain.theme;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 인기_테마_조회_정상_동작_테스트() {
        Long timeId = insertTime("10:00", "11:00");
        Long theme1 = insertTheme("테마1");
        Long theme2 = insertTheme("테마2");

        LocalDate within = LocalDate.now().minusDays(3);
        insertReservation("유저1", within, timeId, theme1);
        Long timeId2 = insertTime("11:00", "12:00");
        insertReservation("유저2", within, timeId2, theme1);
        Long timeId3 = insertTime("12:00", "13:00");
        insertReservation("유저3", within, timeId3, theme2);

        RestAssured.given().log().all()
            .when().get("/themes/top")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(2))
            .body("[0].name", is("테마1"))
            .body("[1].name", is("테마2"));
    }

    @Test
    void 인기_테마_조회_오래된_예약은_집계되지_않는_테스트() {
        Long timeId = insertTime("10:00", "11:00");
        Long themeId = insertTheme("테마1");

        LocalDate pastDate = LocalDate.now().minusDays(30);
        insertReservation("유저1", pastDate, timeId, themeId);

        RestAssured.given().log().all()
            .when().get("/themes/top")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(0));
    }

    @Test
    void 테마_전체_조회_정상_동작_테스트() {
        insertTheme("테마1");
        insertTheme("테마2");

        RestAssured.given().log().all()
            .when().get("/themes")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(2))
            .body("[0].name", is("테마1"))
            .body("[1].name", is("테마2"));
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
            "INSERT INTO theme (name, description, image_url) VALUES (?, ?, ?)",
            name, "설명", "https://example.com/image.jpg"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM theme WHERE name = ?", Long.class, name
        );
    }

    private Long insertTime(String startAt, String finishAt) {
        jdbcTemplate.update(
            "INSERT INTO reservation_time (start_at, finish_at) VALUES (?, ?)",
            startAt, finishAt
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_time WHERE start_at = ?", Long.class, startAt
        );
    }

    private void insertReservation(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
            "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
            name, date, timeId, themeId
        );
    }
}
