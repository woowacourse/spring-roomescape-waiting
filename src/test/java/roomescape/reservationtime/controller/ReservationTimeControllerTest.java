package roomescape.reservationtime.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationTimeControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("존재하지 않는 예약 시간을 삭제하면 404를 응답한다")
    void respondNotFoundWhenDeletingNonExistingReservationTime() {
        RestAssured.given().log().all()
                .when().delete("/times/0")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약 시간을 추가하고 조회하고 삭제한다")
    void createFindAndDeleteReservationTime() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "startAt", "10:00"
                ))
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation_time", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("예약 시작 시간을 입력하지 않으면 400을 응답한다")
    void respondBadRequestWhenReservationStartAtIsMissing() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of())
                .when().post("/times")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("예약 시작 시간을 입력해야 합니다."));
    }

    @Test
    @Sql("/clear.sql")
    @DisplayName("해당 시간에 예약이 있으면 예약 시간 삭제시 409를 응답한다")
    void respondConflictWhenDeletingReservationTimeInUse() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation_slot (reservation_date, time_id, theme_id) VALUES (?, ?, ?)", "2026-08-05", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation (customer_name, customer_email, slot_id) VALUES (?, ?, ?)", "브라운", "brown@example.com", "1");

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(409);
    }
}
