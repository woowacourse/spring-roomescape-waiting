package roomescape.slot.domain;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.ControllerTestSupport;

class SlotApiIntegrationTest extends ControllerTestSupport {

    @Test
    @DisplayName("슬롯을 생성할 수 있다.")
    void creates_slot_successfully() {
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
    @DisplayName("존재하지 않는 시간으로 슬롯 생성 시 404를 응답한다.")
    void creating_slot_with_missing_time_returns_not_found() {
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
    @DisplayName("존재하지 않는 테마로 슬롯 생성 시 404를 응답한다.")
    void creating_slot_with_missing_theme_returns_not_found() {
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
    @DisplayName("전체 슬롯을 조회할 수 있다.")
    void finds_all_slots_successfully() {
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
    @DisplayName("슬롯을 단건 조회할 수 있다.")
    void finds_single_slot_successfully() {
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
    @DisplayName("슬롯을 삭제할 수 있다.")
    void deletes_slot_successfully() {
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
