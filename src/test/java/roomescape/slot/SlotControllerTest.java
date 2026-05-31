package roomescape.slot;

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

@ActiveProfiles("test")
@Import(TestTimeConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class SlotControllerTest {

    @BeforeEach
    void setUp() {
        RestAssured.port = 8080;
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
    void 슬롯_생성() {
        String accessToken = loginManager();
        Map<String, Object> slot = new HashMap<>();
        slot.put("date", "2026-05-06");
        slot.put("timeId", 1);
        slot.put("themeId", 4);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(slot)
                .when().post("/api/manager/slots")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", is(6))
                .body("data.date", is("2026-05-06"))
                .body("data.time_id", is(1))
                .body("data.theme_id", is(4));
    }

    @Test
    void 존재하지_않는_시간으로_슬롯_생성시_404를_응답한다() {
        String accessToken = loginManager();
        Map<String, Object> slot = new HashMap<>();
        slot.put("date", "2026-05-06");
        slot.put("timeId", 999);
        slot.put("themeId", 4);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(slot)
                .when().post("/api/manager/slots")
                .then().log().all()
                .statusCode(404)
                .body("success", is(false))
                .body("error.code", is("RESERVATIONTIME_404"));
    }

    @Test
    void 존재하지_않는_테마로_슬롯_생성시_404를_응답한다() {
        String accessToken = loginManager();
        Map<String, Object> slot = new HashMap<>();
        slot.put("date", "2026-05-06");
        slot.put("timeId", 1);
        slot.put("themeId", 999);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(slot)
                .when().post("/api/manager/slots")
                .then().log().all()
                .statusCode(404)
                .body("success", is(false))
                .body("error.code", is("THEME_404"));
    }

    @Test
    void 슬롯_전체_조회() {
        String accessToken = loginManager();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/manager/slots")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(5));
    }

    @Test
    void 슬롯_단건_조회() {
        String accessToken = loginManager();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/manager/slots/1")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", is(1))
                .body("data.date", is("2026-05-05"))
                .body("data.time_id", is(1))
                .body("data.theme_id", is(1));
    }

    @Test
    void 슬롯_삭제() {
        String accessToken = loginManager();

        Map<String, Object> slot = new HashMap<>();
        slot.put("date", "2026-05-06");
        slot.put("timeId", 1);
        slot.put("themeId", 4);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(slot)
                .when().post("/api/manager/slots")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(6));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().delete("/api/manager/slots/6")
                .then().log().all()
                .statusCode(204);
    }
}
