package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import static io.restassured.http.ContentType.JSON;

import java.time.LocalDate;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ThemeAcceptanceTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART");
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART");
    }

    @Test
    @DisplayName("예약 테마 생성 API")
    void theme_generation_API() {
        // given
        Map<String, String> requestBody = Map.of("name", "theme-1", "description", "this is a new theme",
                "thumbnail", "fun");

        // when & then
        RestAssured
                .given().contentType(JSON).body(requestBody)
                .when().post("/themes")
                .then().statusCode(HttpStatus.SC_CREATED).body("id", is(1));
    }

    @Test
    @DisplayName("예약 테마 목록 조회 API")
    void theme_inquiry_API() {
        // given
        Map<String, String> requestBody = Map.of("name", "theme-1", "description", "this is a new theme",
                "thumbnail", "fun");

        RestAssured
                .given().contentType(JSON).body(requestBody)
                .when().post("/themes")
                .then().statusCode(HttpStatus.SC_CREATED);
        RestAssured
                .given().contentType(JSON).body(requestBody)
                .when().post("/themes")
                .then().statusCode(HttpStatus.SC_CREATED);

        // when & then
        RestAssured
                .given()
                .when().get("/themes")
                .then().statusCode(200).body("size()", is(2));
    }

    @Test
    @DisplayName("예약 테마 삭제 API")
    void theme_remove_API() {
        // given
        Map<String, String> requestBody1 = Map.of("name", "theme-1", "description", "this is a new theme",
                "thumbnail", "fun");
        Map<String, String> requestBody2 = Map.of("name", "theme-1", "description", "this is a new theme",
                "thumbnail", "fun");

        RestAssured
                .given().contentType(JSON).body(requestBody1)
                .when().post("/themes")
                .then().statusCode(HttpStatus.SC_CREATED);
        RestAssured
                .given().contentType(JSON).body(requestBody2)
                .when().post("/themes")
                .then().statusCode(HttpStatus.SC_CREATED);

        // when
        RestAssured
                .when().delete("/themes/1")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);

        // then
        RestAssured
                .given().log().all()
                .when().log().all().get("/themes")
                .then().statusCode(HttpStatus.SC_OK).body("size()", is(1))
                .log().all();
    }

    @Test
    @DisplayName("테마 랭크 조회 API")
    void theme_ranking_inquiry_API() {
        // given
        jdbcTemplate.update("INSERT INTO member(name, email,password, role) VALUES (?,?,?,?)", "aa", "aa@aa.aa", "aa",
                "ADMIN");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "01:00");
        setThemes(11);
        addReservationBy(2, 11);
        addReservationBy(3, 10);
        addReservationBy(1, 9);
        addReservationBy(4, 8);
        addReservationBy(5, 6);
        addReservationBy(7, 5);
        addReservationBy(8, 4);
        addReservationBy(6, 3);
        addReservationBy(9, 2);
        addReservationBy(10, 2);
        addReservationBy(11, 1);

        // when
        RestAssured
                .given()
                .when().get("/themes/ranking")
                .then().body("size()", is(10))
                .body("[0].id", is(2))
                .body("[1].id", is(3))
                .body("[2].id", is(1))
                .body("[3].id", is(4))
                .body("[4].id", is(5))
                .body("[5].id", is(7))
                .body("[6].id", is(8))
                .body("[7].id", is(6));
    }

    private void setThemes(int count) {
        for (int i = 0; i < count; i++) {
            jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail) VALUES (?, ?, ?)", "name", "desc",
                    "thumb");
        }
    }

    private void addReservationBy(int themeId, int count) {
        for (int i = 1; i <= count; i++) {
            jdbcTemplate.update("INSERT INTO reservation(date, theme_id, time_id, member_id) VALUES (?,?,?,?)",
                    LocalDate.now().minusDays(count % 7), themeId, "1", "1");
        }
    }

}
