package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Import(TestTimeConfig.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AcceptanceTest {

    @BeforeEach
    void setUp() {
        RestAssured.port = 8080;
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

    private String userAToken() {
        return login("a", "test1");
    }

    private String userBToken() {
        return login("b", "test2");
    }

    private String managerToken() {
        return login("d", "test4");
    }

    @Test
    @DisplayName("시간 조회 후 예약/대기 신청 시 내 예약 목록에서 확정 예약과 대기가 함께 조회되고 대기 순번이 표시된다.")
    void 시간조회_예약_대기_내목록_통합시나리오() {
        String userAToken = userAToken();
        String userBToken = userBToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userAToken)
                .queryParam("date", "2026-05-05")
                .queryParam("themeId", 4)
                .when().get("/api/user/times/availability")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].isAvailable", is(true));

        Map<String, Object> reservationRequest = new HashMap<>();
        reservationRequest.put("date", "2026-05-05");
        reservationRequest.put("timeId", 4);
        reservationRequest.put("themeId", 4);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userAToken)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/api/user/reservations")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true));

        Map<String, Object> waitingRequest = new HashMap<>();
        waitingRequest.put("date", "2026-05-05");
        waitingRequest.put("timeId", 4);
        waitingRequest.put("themeId", 4);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userBToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.waitingOrder", is(1));

        List<Map<String, Object>> userBMyList = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userBToken)
                .queryParam("period", "UPCOMING")
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .extract()
                .path("data");

        assertThat(userBMyList).extracting(item -> item.get("status"))
                .contains("RESERVED", "WAITING");

        assertThat(userBMyList)
                .anySatisfy(item -> {
                    if (!"WAITING".equals(item.get("status"))) {
                        return;
                    }
                    assertThat(item.get("date")).isEqualTo("2026-05-05");
                    Map<String, Object> theme = (Map<String, Object>) item.get("theme");
                    Map<String, Object> time = (Map<String, Object>) item.get("time");
                    assertThat(theme.get("id")).isEqualTo(4);
                    assertThat(time.get("id")).isEqualTo(4);
                    assertThat(item.get("waitingOrder")).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("예약자가 예약을 취소하면 같은 슬롯의 선두 대기자가 자동으로 예약 승격된다.")
    void 예약_삭제시_대기_자동승격() {
        String userAToken = userAToken();
        String userBToken = userBToken();

        Map<String, Object> waitingRequest = new HashMap<>();
        waitingRequest.put("date", "2026-05-05");
        waitingRequest.put("timeId", 1);
        waitingRequest.put("themeId", 1);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userBToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", notNullValue())
                .body("data.waitingOrder", is(1));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userAToken)
                .when().delete("/api/user/reservations/1")
                .then().log().all()
                .statusCode(204);

        List<Map<String, Object>> myReservations = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userBToken)
                .queryParam("period", "UPCOMING")
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("data");

        assertThat(myReservations)
                .anySatisfy(item -> {
                    assertThat(item.get("status")).isEqualTo("RESERVED");
                    assertThat(item.get("date")).isEqualTo("2026-05-05");
                    Map<String, Object> theme = (Map<String, Object>) item.get("theme");
                    Map<String, Object> time = (Map<String, Object>) item.get("time");
                    assertThat(theme.get("id")).isEqualTo(1);
                    assertThat(time.get("id")).isEqualTo(1);
                    assertThat(item.get("waitingOrder")).isNull();
                });
        assertThat(myReservations)
                .noneSatisfy(item -> assertThat(item.get("status")).isEqualTo("WAITING"));
    }

    @Test
    @DisplayName("예약자가 다른 슬롯으로 예약을 수정하면 기존 슬롯의 선두 대기자가 자동으로 예약 승격된다.")
    void 예약_수정시_기존슬롯_대기_자동승격() {
        String userAToken = userAToken();
        String userBToken = userBToken();
        String managerToken = managerToken();

        Map<String, Object> createScheduleRequest = new HashMap<>();
        createScheduleRequest.put("date", "2026-05-06");
        createScheduleRequest.put("timeId", 1);
        createScheduleRequest.put("themeId", 2);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + managerToken)
                .contentType(ContentType.JSON)
                .body(createScheduleRequest)
                .when().post("/api/manager/schedules")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true));

        Map<String, Object> waitingRequest = new HashMap<>();
        waitingRequest.put("date", "2026-05-05");
        waitingRequest.put("timeId", 2);
        waitingRequest.put("themeId", 2);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userBToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.waitingOrder", is(1));

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("date", "2026-05-06");
        updateRequest.put("timeId", 1);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userAToken)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when().patch("/api/user/reservations/2")
                .then().log().all()
                .statusCode(200);

        List<Map<String, Object>> myReservations = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userBToken)
                .queryParam("period", "UPCOMING")
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("data");

        assertThat(myReservations)
                .anySatisfy(item -> {
                    assertThat(item.get("status")).isEqualTo("RESERVED");
                    assertThat(item.get("date")).isEqualTo("2026-05-05");
                    Map<String, Object> theme = (Map<String, Object>) item.get("theme");
                    Map<String, Object> time = (Map<String, Object>) item.get("time");
                    assertThat(theme.get("id")).isEqualTo(2);
                    assertThat(time.get("id")).isEqualTo(2);
                    assertThat(item.get("waitingOrder")).isNull();
                });
        assertThat(myReservations)
                .noneSatisfy(item -> assertThat(item.get("status")).isEqualTo("WAITING"));
    }
}
