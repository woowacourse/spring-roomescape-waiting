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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationE2ETest {

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

        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "무서운 테마", "thumb.png");
    }

    @Test
    @DisplayName("GET /admin/reservations - 전체 예약 목록을 조회한다")
    void getReservations() {
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", "2025-12-25", 1L, 1L);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", is("브라운"))
                .body("reservations[0].date", is("2025-12-25"))
                .body("reservations[0].time.id", is(1))
                .body("reservations[0].theme.id", is(1))
                .body("reservations[0].theme.name", is("공포"));
    }

    @Test
    @DisplayName("POST /reservations - 예약을 생성하면 201과 ResponseReservation을 반환한다")
    void createReservation() {
        String futureDate = LocalDate.now().plusDays(7).toString();
        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", futureDate);
        body.put("timeId", 1);
        body.put("themeId", 1);
        body.put("amount", 50000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("브라운"))
                .body("date", is(futureDate))
                .body("time.id", is(1))
                .body("theme.id", is(1));
    }

    @Test
    @DisplayName("PATCH /reservations/{id}?name=... - 본인 예약 날짜와 시간을 변경한다")
    void updateReservation() {
        String originalDate = LocalDate.now().plusDays(7).toString();
        String updateDate = LocalDate.now().plusDays(8).toString();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "12:00");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", originalDate, 1L, 1L);
        Map<String, Object> body = new HashMap<>();
        body.put("date", updateDate);
        body.put("timeId", 2);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("브라운"))
                .body("date", is(updateDate))
                .body("time.id", is(2))
                .body("time.startAt", is("12:00"))
                .body("theme.id", is(1));
    }

    @Test
    @DisplayName("DELETE /reservations/{id}?name=... - 본인 예약을 삭제하면 목록에서 제거된다")
    void deleteReservation() {
        String futureDate = LocalDate.now().plusDays(7).toString();
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", futureDate, 1L, 1L);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0));
    }

    @Test
    @DisplayName("DELETE /reservations/{id}?name=... - 지난 예약은 삭제할 수 없다")
    void deleteReservation_pastReservation() {
        String pastDate = LocalDate.now().minusDays(1).toString();
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", pastDate, 1L, 1L);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(409)
                .body("code", is("DOMAIN_CONFLICT"));
    }

    @Test
    @DisplayName("POST /reservations - 존재하지 않는 시간 ID로 예약을 생성하면 404를 반환한다")
    void createReservation_invalidTimeId() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2025-12-25");
        body.put("timeId", 999);
        body.put("themeId", 1);
        body.put("amount", 50000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }
}
