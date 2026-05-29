package roomescape.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.List;
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
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')"); // id=1
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('14:00', '15:00')"); // id=2
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com')"); // id=1
    }

    @Test
    @DisplayName("[버그] 예약 수정 시 대기자가 새 슬롯을 따라간다")
    void 예약_수정_시_대기자가_새_슬롯을_따라간다() {
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

        List<Map<String, Object>> beforeUpdate = RestAssured.given().log().all()
                .when().get("/waitings?name=user2")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        assertThat(beforeUpdate).hasSize(1);
        assertThat(beforeUpdate.get(0).get("date")).isEqualTo("2099-12-01");

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("date", "2099-12-02");
        updateRequest.put("timeId", 2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(200);

        List<Map<String, Object>> afterUpdate = RestAssured.given().log().all()
                .when().get("/waitings?name=user2")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("$");

        assertThat(afterUpdate).hasSize(1);
        assertThat(afterUpdate.get(0).get("date")).isEqualTo("2099-12-01");
    }
}
