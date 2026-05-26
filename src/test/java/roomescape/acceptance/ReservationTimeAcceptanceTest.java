package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void GET_times_목록을_조회한다() {
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES ('10:00')");

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(1));
    }

    @Test
    void GET_times_id_단건을_조회한다() {
        jdbcTemplate.update("INSERT INTO reservation_time(id, start_at) VALUES (1, '10:00')");

        RestAssured.given().log().all()
                .when().get("/times/1")
                .then().log().all()
                .statusCode(200)
                .body("startAt", equalTo("10:00"));
    }

    @Test
    void GET_times_id_없는_id면_404과_메시지를_반환한다() {
        RestAssured.given().log().all()
                .when().get("/times/9999")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("예약 시간을(를) 찾을 수 없습니다. id=9999"));
    }
}
