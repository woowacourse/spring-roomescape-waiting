package roomescape.reservation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

@Import(TestTimeConfig.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationControllerTest {

    @BeforeEach
    void setUp() {
        RestAssured.port = 8080;
    }

    private String loginUser() {
        return login("a", "test1");
    }

    private String loginManager() {
        return login("d", "test4");
    }

    private String login(String name, String password) {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("name", name);
        loginRequest.put("password", password);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/api/login")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("data.accessToken");
    }

    private Map<String, Object> reservationRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("memberId", 1);
        reservation.put("date", "2026-05-05");
        reservation.put("timeId", 4);
        reservation.put("themeId", 4);
        return reservation;
    }

    @Test
    void 예약_생성() {
        String accessToken = loginUser();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(reservationRequest())
                .when().post("/api/user/reservations")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", is(6))
                .body("data.memberId", is(1))
                .body("data.scheduleId", is(4));
    }

    @Test
    void 나의_특정_예약_삭제_및_나의_예약_목록_조회() {
        String accessToken = loginUser();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(4));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("id", 1)
                .when().delete("/api/user/reservations/{id}")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(3));
    }

    @Test
    void 나의_예약_조회시_period가_upcoming이면_다가오는_예약과_대기를_조회한다() {
        String accessToken = loginUser();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("period", "UPCOMING")
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(4));
    }

    @Test
    void 나의_예약_조회시_period가_history이면_지난_예약과_대기를_조회한다() {
        String accessToken = loginUser();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("period", "HISTORY")
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(0));
    }

    @Test
    void 매니저_예약_목록_조회() {
        String accessToken = loginManager();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/manager/reservations")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(5));
    }

    @Test
    void 매니저_예약_삭제() {
        String accessToken = loginManager();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("id", 1)
                .when().delete("/api/manager/reservations/{id}")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 매니저_예약_수정() {
        String accessToken = loginManager();

        Map<String, Object> createScheduleRequest = new HashMap<>();
        createScheduleRequest.put("date", "2026-05-05");
        createScheduleRequest.put("themeId", 1L);
        createScheduleRequest.put("timeId", 2L);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(createScheduleRequest)
                .when().post("/api/manager/schedules")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("date", "2026-05-05");
        updateRequest.put("timeId", 2);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .pathParam("id", 1)
                .when().patch("/api/manager/reservations/{id}")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", is(1))
                .body("data.memberId", is(1));
    }
}
