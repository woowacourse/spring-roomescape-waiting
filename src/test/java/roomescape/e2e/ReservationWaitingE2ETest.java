package roomescape.e2e;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationWaitingE2ETest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('14:00', '15:00')");
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url, price) VALUES ('테마A', '설명A', 'https://a.com', 10000)");
    }

    @Test
    @DisplayName("자신의 예약에는 대기할 수 없다.")
    void 자신의_예약에는_대기할_수_없다() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "user1");
        reservation.put("date", "2099-12-01");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        Integer reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().path("id");

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("name", "user1");
        waiting.put("reservationId", reservationId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("자기 예약에는 대기할 수 없습니다."));
    }

    @Test
    @DisplayName("관리자가 수동으로 대기를 예약으로 승격할 수 있다.")
    void 관리자_대기_승격() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "user1");
        reservation.put("date", "2099-12-01");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        Integer reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().path("id");

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("name", "user2");
        waiting.put("reservationId", reservationId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);

        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", reservationId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .when().post("/admin/waitings/approve/" + reservationId)
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo("user2"));

        RestAssured.given().log().all()
                .when().get("/waitings?name=user2")
                .then().log().all()
                .statusCode(200)
                .body("$", hasSize(0));
    }
}
