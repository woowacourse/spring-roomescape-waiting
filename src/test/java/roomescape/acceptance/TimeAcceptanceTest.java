package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import java.time.LocalTime;
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
import io.restassured.http.ContentType;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TimeAcceptanceTest {
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
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART");
    }

    @Test
    @DisplayName("예약 시간 생성 API")
    void reservation_time_generation_API() {
        // given
        Map<String, LocalTime> startAt = Map.of("startAt", LocalTime.of(1, 0));

        // when & then
        RestAssured
                .given().contentType(ContentType.JSON).body(startAt)
                .when().post("/times")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1))
                .log().all();
    }

    @Test
    @DisplayName("예약 시간 조회 API")
    void reservation_time_inquiry_API() {
        // given
        Map<String, LocalTime> startAt1 = Map.of("startAt", LocalTime.of(1, 0));
        Map<String, LocalTime> startAt2 = Map.of("startAt", LocalTime.of(2, 0));

        RestAssured
                .given().contentType(ContentType.JSON).body(startAt1)
                .when().post("/times")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1))
                .log().all();

        RestAssured
                .given().contentType(ContentType.JSON).body(startAt2)
                .when().post("/times")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(2))
                .log().all();

        // when & then
        RestAssured
                .given()
                .when().get("/times")
                .then().statusCode(HttpStatus.SC_OK)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("예약 시간 삭제 API")
    void reservation_time_remove_API() {
        // given
        Map<String, LocalTime> startAt1 = Map.of("startAt", LocalTime.of(1, 0));
        Map<String, LocalTime> startAt2 = Map.of("startAt", LocalTime.of(2, 0));
        RestAssured
                .given().contentType(ContentType.JSON).body(startAt1)
                .when().post("/times")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1))
                .log().all();

        RestAssured
                .given().contentType(ContentType.JSON).body(startAt2)
                .when().post("/times")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(2))
                .log().all();

        // when
        RestAssured
                .given()
                .when().delete("/times/1")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);

        // then
        RestAssured
                .given()
                .when().get("/times")
                .then().statusCode(HttpStatus.SC_OK)
                .body("size()", is(1));
    }

    // GET localhost:8080/times/available?date=2024-10-11&theme-id=1
    @Test
    @DisplayName("예약 가능 시간 조회 API")
    void inquire_available_time_API() {
        // given
        jdbcTemplate.update("INSERT INTO member (name, email, password, role) VALUES (?,?,?,?)", "aa", "aa@aa.aa", "aa",
                "NORMAL");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "01:00");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "02:00");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "03:00");
        jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail) VALUES (?, ?, ?)", "n", "d", "t");

        jdbcTemplate.update("INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?)",
                "2023-12-11", "1", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?)",
                "2023-12-11", "2", "1", "1");

        // when
        RestAssured
                .given()
                .when().get("/times/available?date=2023-12-11&theme-id=1")
                .then().statusCode(HttpStatus.SC_OK)
                .body("[0].alreadyBooked", is(true))
                .body("[1].alreadyBooked", is(true))
                .body("[2].alreadyBooked", is(false))
                .log().all();
    }
}
