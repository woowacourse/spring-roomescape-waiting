package roomescape.waiting;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;

@Import(TestTimeConfig.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class WaitingControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private String loginUser() {
        return login("b", "test2");
    }

    private String loginOtherUser() {
        return login("c", "test3");
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

    private Map<String, Object> waitingRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("date", LocalDate.of(2026, 5, 5));
        request.put("timeId", 1L);
        request.put("themeId", 1L);
        return request;
    }

    private Integer createWaiting(String accessToken) {
        return RestAssured.given().log().all()
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
    }

    @Test
    @DisplayName("이미 예약된 스케줄에 대기를 신청할 수 있다.")
    void 예약_대기_API_테스트() {
        String accessToken = loginUser();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", notNullValue())
                .body("data.memberId", is(2))
                .body("data.scheduleId", is(1))
                .body("data.waitingOrder", is(1));
    }

    @Test
    @DisplayName("대기를 취소할 수 있다.")
    void 대기_취소_API_테스트() {
        String accessToken = loginUser();
        Integer waitingId = createWaiting(accessToken);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .pathParam("id", waitingId)
                .when().delete("/api/user/waitings/{id}")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("같은 사용자가 같은 스케줄에 중복 대기를 신청할 수 없다.")
    void rejectDuplicateWaitingForSameSlotBySameMember() {
        String accessToken = loginUser();
        createWaiting(accessToken);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(409)
                .body("success", is(false))
                .body("error.code", is("WAITING_409"));
    }

    @Test
    @DisplayName("같은 스케줄에 대기를 신청하면 신청 순서대로 순번이 부여된다.")
    void assignWaitingOrderByRequestSequence() {
        String accessToken = loginUser();
        String otherAccessToken = loginOtherUser();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.memberId", is(2))
                .body("data.scheduleId", is(1))
                .body("data.waitingOrder", is(1));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + otherAccessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.memberId", is(3))
                .body("data.scheduleId", is(1))
                .body("data.waitingOrder", is(2));
    }

    @Test
    @DisplayName("다른 사용자의 대기는 취소할 수 없다.")
    void rejectDeletingOtherMembersWaiting() {
        String accessToken = loginUser();
        String otherAccessToken = loginOtherUser();
        Integer waitingId = createWaiting(accessToken);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + otherAccessToken)
                .contentType(ContentType.JSON)
                .pathParam("id", waitingId)
                .when().delete("/api/user/waitings/{id}")
                .then().log().all()
                .statusCode(403)
                .body("success", is(false))
                .body("error.code", is("WAITING_403_OWNER"));
    }
}
