package roomescape.reservationtime;

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
import java.util.Map;

import static org.hamcrest.Matchers.is;

@Import(TestTimeConfig.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationTimeControllerTest {

    @BeforeEach
    void setUp() {
        RestAssured.port = 8080;
    }

    private String loginUser() {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("name", "a");
        loginRequest.put("password", "test1");

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/api/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("data.accessToken");
    }

    private String loginManager() {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("name", "d");
        loginRequest.put("password", "test4");

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/api/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("data.accessToken");
    }

    @Test
    void 특정날짜와_테마에_예약_가능_시간들_조회_API() {
        String accessToken = loginUser();

        Map<String, Object> options = new HashMap<>();
        options.put("date", "2026-05-05");
        options.put("themeId", 1);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .params(options)
                .when().get("/api/user/times/availability")
                .then().log().all()
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].isAvailable", is(false))
                .statusCode(200);
    }

    @Test
    @DisplayName("예약 가능 시간 조회 및 예약 생성 이후 예약 가능 시간을 재조회를 할 수 있다.")
    void 정상_흐름_테스트() {
        String userToken = loginUser();

        Map<String, Object> options = new HashMap<>();
        options.put("date", "2026-05-05");
        options.put("themeId", 4);

        // 2026-05-05 4번 테마 조회
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userToken)
                .params(options)
                .when().get("/api/user/times/availability")
                .then().log().all()
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].isAvailable", is(true))
                .statusCode(200);

        // 예약 생성
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2026-05-05");
        reservation.put("timeId", 4);
        reservation.put("themeId", 4);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/api/user/reservations")
                .then().log().all()
                .statusCode(201);

        // 특정날짜와_테마에_예약_가능_시간을_조회
        Map<String, Object> options1 = new HashMap<>();
        options1.put("date", "2026-05-05");
        options1.put("themeId", 4);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userToken)
                .params(options1)
                .when().get("/api/user/times/availability")
                .then().log().all()
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].isAvailable", is(false))
                .statusCode(200);
    }

    @Test
    void 매니저_시간_관리_API() {
        String accessToken = loginManager();

        Map<String, String> params = new HashMap<>();
        params.put("startAt", "14:00");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/manager/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/manager/times")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(5));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().delete("/api/manager/times/5")
                .then().log().all()
                .statusCode(204);
    }
}
