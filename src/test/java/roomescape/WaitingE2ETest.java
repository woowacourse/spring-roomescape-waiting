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
import java.util.Map;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WaitingE2ETest {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "무서운 테마", "thumb.png");
    }

    @Test
    @DisplayName("POST /waitings - 예약대기를 생성하면 201과 대기 순번을 반환한다")
    void createWaiting() {
        String futureDate = LocalDate.now().plusDays(1).toString();

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "브라운", "date", futureDate, "timeId", 1, "themeId", 1, "amount", 50000))
                .when().post("/reservations");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "레서",
                        "date", futureDate,
                        "timeId", 1,
                        "themeId", 1))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("레서"))
                .body("date", equalTo(futureDate))
                .body("time.startAt", equalTo("10:00"))
                .body("theme.name", equalTo("공포"))
                .body("order", equalTo(1));
    }

    @Test
    @DisplayName("GET /reservations?name=... - 내 예약과 예약대기 목록을 함께 조회한다")
    void getMyReservations() {
        String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "레서", futureDate, 1L, 1L);
        jdbcTemplate.update("INSERT INTO waiting (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "밍구", futureDate, 1L, 1L);
        jdbcTemplate.update("INSERT INTO waiting (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "레서", futureDate, 1L, 1L);

        RestAssured.given().log().all()
                .queryParam("name", "레서")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("userReservations.size()", is(2))
                .body("userReservations[0].status", is("RESERVED"))
                .body("userReservations[1].status", is("WAITING"))
                .body("userReservations[1].rank", is(2));
    }

    @Test
    @DisplayName("DELETE /waitings/{id} - 예약대기를 삭제하면 204를 반환한다")
    void deleteWaiting() {
        String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO waiting (id, name, date, time_id, theme_id) VALUES (?, ?, ?, ?, ?)",
                1L, "레서", futureDate, 1L, 1L);

        RestAssured.given().log().all()
                .queryParam("name", "레서")
                .when().delete("/waitings/1")
                .then().log().all()
                .statusCode(204);
    }
}
