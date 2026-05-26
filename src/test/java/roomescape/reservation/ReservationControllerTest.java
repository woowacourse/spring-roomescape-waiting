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
import static org.hamcrest.Matchers.notNullValue;

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

    private String login(String name, String password) {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("name", name);
        loginRequest.put("password", password);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/api/login")
                .then().log().all()
                .statusCode(200)
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

    private Map<String, Object> waitingRequest() {
        Map<String, Object> waiting = new HashMap<>();
        waiting.put("date", "2026-05-05");
        waiting.put("timeId", 1);
        waiting.put("themeId", 1);
        return waiting;
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
                .body("data.id", is(5))
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
    void 나의_예약_목록에서_대기도_함께_조회한다() {
        String accessToken = login("b", "test2");

        Integer waitingId = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", notNullValue())
                .extract()
                .path("data.id");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].id", is(waitingId))
                .body("data[0].status", is("WAITING"))
                .body("data[0].waitingOrder", is(1));
    }
}
