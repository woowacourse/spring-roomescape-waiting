package roomescape.slot;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import roomescape.support.ControllerTestSupport;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

class SlotApiIntegrationTest extends ControllerTestSupport {

    @Test
    void 슬롯_생성() {
        String accessToken = loginManagerToken();
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
        String accessToken = loginManagerToken();
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
        String accessToken = loginManagerToken();
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
        String accessToken = loginManagerToken();

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
        String accessToken = loginManagerToken();

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
        String accessToken = loginManagerToken();

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
