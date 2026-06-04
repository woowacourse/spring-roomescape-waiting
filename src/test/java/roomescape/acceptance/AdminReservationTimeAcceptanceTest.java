package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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
    void POST_admin_times_시간을_생성한다() {
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
    void POST_admin_times_본문의_startAt이_누락되면_400과_메시지를_반환한다() {
        Map<String, Object> body = Map.of();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("startAt은(는) 필수 입력값입니다."));
    }

    @Test
    void DELETE_admin_times_id_시간을_삭제한다() {
        long timeId = Scenario.timeNotInUse(jdbcTemplate);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/times/" + timeId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void DELETE_admin_times_없는_id면_404과_메시지를_반환한다() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/times/9999")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("예약 시간을(를) 찾을 수 없습니다. id=9999"));
    }

    @Test
    void DELETE_admin_times_참조하는_예약이_존재하면_409과_메시지를_반환한다() {
        long timeId = Scenario.timeInUse(jdbcTemplate);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/times/" + timeId)
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("예약이 존재하는 시간은 삭제할 수 없습니다."));
    }
}
