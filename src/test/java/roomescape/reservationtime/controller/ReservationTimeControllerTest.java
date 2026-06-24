package roomescape.reservationtime.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
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
class ReservationTimeControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('14:00', '15:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('18:00', '19:00')");
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url, price) VALUES ('테마A', '설명A', 'https://a.com', 10000)");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', ?, 1, 1)",
                LocalDate.now().minusDays(1));
    }

    @Test
    @DisplayName("시간 생성 성공")
    void 시간_생성_성공() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "20:00", "finishAt", "21:00"))
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", equalTo("20:00:00"));
    }

    @Test
    @DisplayName("시간 전체 조회 성공")
    void 시간_전체_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }

    @Test
    @DisplayName("시간 삭제 성공")
    void 시간_삭제_성공() {
        Integer id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "20:00", "finishAt", "21:00"))
                .when().post("/times")
                .then().extract().path("id");

        RestAssured.given().log().all()
                .when().delete("/times/" + id)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("예약 가능 시간 조회 성공")
    void 예약_가능_시간_조회_성공() {
        String date = LocalDate.now().minusDays(1).toString();
        RestAssured.given().log().all()
                .when().get("/times/available?date=" + date + "&themeId=1")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }
}
