package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ThemeE2ETest {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    @DisplayName("GET /themes - 테마 목록을 조회한다")
    void findThemes() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "무서움", "thumb1.png");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "추리", "지능", "thumb2.png");

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(2))
                .body("themes.name", contains("공포", "추리"));
    }

    @Test
    @DisplayName("GET /themes/{id}/available-times - 예약된 시간은 isAvailable=false, 예약 안 된 시간은 true")
    void findAvailableTimes() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "무서움", "thumb.png");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", "2026-05-07", 1L, 1L);

        RestAssured.given().log().all()
                .when().get("/themes/1/available-times?date=2026-05-07")
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(2))
                .body("times.find { it.id == 1 }.isAvailable", is(false))
                .body("times.find { it.id == 2 }.isAvailable", is(true));
    }

    @Test
    @DisplayName("GET /themes/popular - 최근 7일 예약 수 기준으로 인기 테마를 반환한다")
    void findPopularThemes() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "무서움", "thumb1.png");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "추리", "지능", "thumb2.png");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "12:00");

        String recentDate = LocalDate.now().minusDays(3).toString();
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "A", recentDate, 1L, 1L);
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "B", recentDate, 1L, 2L);
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "C", recentDate, 2L, 2L);

        RestAssured.given().log().all()
                .when().get("/themes/popular?days=7&limit=10")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(2))
                .body("themes[0].name", is("추리"))
                .body("themes[0].rank", is(1))
                .body("themes[1].name", is("공포"))
                .body("themes[1].rank", is(2));
    }

    @Test
    @DisplayName("POST /admin/themes - 테마를 생성하면 200과 생성된 테마를 반환한다")
    void createTheme() {
        Map<String, String> body = new HashMap<>();
        body.put("name", "공포");
        body.put("description", "무서움");
        body.put("thumbnail", "thumb.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("공포"))
                .body("description", is("무서움"))
                .body("thumbnail", is("thumb.png"));
    }

    @Test
    @DisplayName("DELETE /admin/themes/{id} - 테마를 삭제하면 204를 반환한다")
    void deleteTheme() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "무서움", "thumb.png");

        RestAssured.given().log().all()
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(0));
    }
}
