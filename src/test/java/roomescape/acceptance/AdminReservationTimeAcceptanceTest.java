package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Scenario;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationTimeAcceptanceTest {

    private static final String AUTHORIZATION = "Authorization";
    private static final String MANAGER_NAME = "관리자";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private String managerBearer() {
        return DbFixtures.managerBearer(jdbcTemplate, MANAGER_NAME);
    }

    @Test
    @DisplayName("POST /admin/times - 시간을 생성한다")
    void createReservationTime() {
        Map<String, Object> body = Map.of("startAt", "10:00");

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .header("Location", matchesPattern("/times/\\d+"));
    }

    @Test
    @DisplayName("POST /admin/times - 본문의 startAt이 누락되면 400과 메시지를 반환한다")
    void createReservationTimeReturns400WhenStartAtIsMissing() {
        Map<String, Object> body = Map.of();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("DELETE /admin/times/{id} - 시간을 삭제한다")
    void deleteReservationTime() {
        long timeId = Scenario.timeNotInUse(jdbcTemplate);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/times/" + timeId)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("DELETE /admin/times - 없는 id면 404과 메시지를 반환한다")
    void deleteReservationTimeReturns404WhenIdDoesNotExist() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/times/9999")
                .then().log().all()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /admin/times - 참조하는 예약이 존재하면 409과 메시지를 반환한다")
    void deleteReservationTimeReturns409WhenReferencedByReservation() {
        long timeId = Scenario.timeInUse(jdbcTemplate);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/times/" + timeId)
                .then().log().all()
                .statusCode(409)
                .body("code", equalTo("RESERVATION_TIME_IN_USE"));
    }
}
