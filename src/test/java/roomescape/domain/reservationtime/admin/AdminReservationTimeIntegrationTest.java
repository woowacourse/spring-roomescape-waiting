package roomescape.domain.reservationtime.admin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AdminReservationTimeIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${token}")
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_date");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    @DisplayName("관리자의 예약 시간 전체 조회를 end-to-end로 확인한다.")
    void getAllReservationTime() {
        jdbcTemplate.update(
            "INSERT INTO reservation_time(start_at) VALUES (?)",
            "10:10"
        );

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/times")
            .then().log().all()
            .statusCode(200)
            .body("[0].startAt", is("10:10"));
    }

    @Test
    @DisplayName("관리자가 토큰을 누락했을 경우 401 예외가 발생한다.")
    void getAllReservationTimeWithoutToken() {
        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/admin/times")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    @DisplayName("관리자의 예약 시간 생성을 end-to-end로 확인한다.")
    void createReservationTime() {
        String request = """
            {
                "startAt": "10:10"
            }
            """;

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .body(request)
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(200)
            .body("startAt", is("10:10"));

        given()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/times")
            .then()
            .statusCode(200)
            .body("startAt", hasItem("10:10"));
    }

    @Test
    @DisplayName("관리자 예약 시간 생성 시 시작 시간이 누락되었을 경우 400 에러가 발생한다.")
    void createReservationTimeWithoutStartAt() {
        String request = """
            {
            }
            """;

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .body(request)
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(400)
            .body("code", is("INPUT_VALIDATION_ERROR"))
            .body("message", is("시간은 필수 사항 입니다. 시간을 선택해주세요."));
    }

    @Test
    @DisplayName("관리자 예약 시간 생성 시 관리자 인증 토큰이 누락되었을 경우 401 에러가 발생한다.")
    void createReservationTimeWithoutToken() {
        String request = """
            {
                "startAt": "10:10"
            }
            """;

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(401);
    }

    @Test
    @DisplayName("관리자의 예약 시간 삭제를 end-to-end로 확인한다.")
    void deleteReservationTime() {
        jdbcTemplate.update(
            "INSERT INTO reservation_time(start_at) VALUES (?)",
            "10:10"
        );

        Long timeId = jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_time WHERE start_at = ?",
            Long.class,
            "10:10:00"
        );

        given().log().all()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().delete("/admin/times/{id}", timeId)
            .then().log().all()
            .statusCode(200);

        given()
            .contentType(ContentType.JSON)
            .header("X-ADMIN-TOKEN", adminToken)
            .when().get("/admin/times")
            .then()
            .statusCode(200)
            .body("startAt", not(hasItem("10:10")));
    }
}
