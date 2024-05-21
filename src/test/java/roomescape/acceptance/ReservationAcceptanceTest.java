package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

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
import io.restassured.http.ContentType;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.service.security.JwtProvider;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationAcceptanceTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JwtProvider jwtProvider;

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
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART");
    }

    @Test
    @DisplayName("예약 생성 API")
    void member_generate_API() {
        // given
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "name", "desc",
                "thumb");
        jdbcTemplate.update("INSERT INTO member(name, email, password, role) VALUES (?, ?, ?, ?)", "fram", "aa@aa.aa",
                "aa", "NORMAL");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "01:00");
        Map<String, String> reservationBody = Map.of("date", LocalDate.now().plusDays(1).toString(), "themeId", "1",
                "memberId", "1", "timeId",
                "1");
        String encodedMember = jwtProvider.encode(new Member(1L, "fram", "aa@aa.aa", "aa", Role.NORMAL));

        // when & then
        RestAssured
                .given().contentType(ContentType.JSON).body(reservationBody).cookie("token", encodedMember)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1));
    }

    @Test
    @DisplayName("예약 조회 API")
    void reservation_inquiry_API() {
        // given
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "name", "desc",
                "thumb");
        jdbcTemplate.update("INSERT INTO member(name, email, password, role) VALUES (?, ?, ?, ?)", "fram", "aa@aa.aa",
                "aa", "NORMAL");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "01:00");
        Map<String, String> reservationBody1 = Map.of("date", "2024-12-11", "themeId", "1", "memberId", "1", "timeId",
                "1");
        Map<String, String> reservationBody2 = Map.of("date", "2024-12-12", "themeId", "1", "memberId", "1", "timeId",
                "1");
        String encodedMember = jwtProvider.encode(new Member(1L, "fram", "aa@aa.aa", "aa", Role.NORMAL));

        RestAssured
                .given().contentType(ContentType.JSON).body(reservationBody1).cookie("token", encodedMember)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1));

        RestAssured
                .given().contentType(ContentType.JSON).body(reservationBody2).cookie("token", encodedMember)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(2));

        // when & then
        RestAssured
                .given()
                .when().get("/reservations")
                .then().statusCode(HttpStatus.SC_OK)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("예약 삭제 API")
    void reservation_remove_API() {
        // given
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "name", "desc",
                "thumb");
        jdbcTemplate.update("INSERT INTO member(name, email, password, role) VALUES (?, ?, ?, ?)", "fram", "aa@aa.aa",
                "aa", "NORMAL");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "01:00");
        Map<String, String> reservationBody1 = Map.of("date", "2024-12-11", "themeId", "1", "memberId", "1", "timeId",
                "1");
        Map<String, String> reservationBody2 = Map.of("date", "2024-12-12", "themeId", "1", "memberId", "1", "timeId",
                "1");
        String encodedMember = jwtProvider.encode(new Member(1L, "fram", "aa@aa.aa", "aa", Role.NORMAL));

        RestAssured
                .given().contentType(ContentType.JSON).body(reservationBody1).cookie("token", encodedMember)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1));

        RestAssured
                .given().contentType(ContentType.JSON).body(reservationBody2).cookie("token", encodedMember)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(2));
        // when
        RestAssured
                .given()
                .when().delete("/reservations/1")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);

        // when & then
        RestAssured
                .given()
                .when().get("/reservations")
                .then().statusCode(HttpStatus.SC_OK)
                .body("size()", is(1))
                .body("[0].id", is(2));
    }
}
