package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("GET /times - 목록을 조회한다")
    void getReservationTimes() {
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES ('10:00')");

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(1));
    }

    @Test
    @DisplayName("GET /times/{id} - 단건을 조회한다")
    void getReservationTime() {
        jdbcTemplate.update("INSERT INTO reservation_time(id, start_at) VALUES (1, '10:00')");

        RestAssured.given().log().all()
                .when().get("/times/1")
                .then().log().all()
                .statusCode(200)
                .body("startAt", equalTo("10:00"));
    }

    @Test
    @DisplayName("GET /times/{id} - 없는 id면 404과 메시지를 반환한다")
    void getReservationTimeReturns404WhenIdDoesNotExist() {
        RestAssured.given().log().all()
                .when().get("/times/9999")
                .then().log().all()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
    }
}
