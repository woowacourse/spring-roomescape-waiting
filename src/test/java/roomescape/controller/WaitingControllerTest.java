package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import roomescape.AcceptanceTest;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class WaitingControllerTest extends AcceptanceTest {

    @Test
    void 대기를_생성한다() {
        long timeId = createTime("10:00");
        long themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        createReservation("브라운", "2026-05-05", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "조이");
        params.put("date", "2026-05-05");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        String location = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("조이"))
                .body("status", equalTo("WAITING"))
                .extract()
                .header("Location");
    }

    @Test
    void 동일한_슬롯에_대기할_수_없다() {
        long timeId = createTime("10:00");
        long themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        createReservation("브라운", "2026-05-05", timeId, themeId);
        createWaiting("조이", "2026-05-05", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "조이");
        params.put("date", "2026-05-05");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(409)
                .body("exceptionCode", equalTo("WAITING_ALREADY_EXISTS"));
    }

    @Test
    void 본인_예약에_대기할_수_없다() {
        long timeId = createTime("10:00");
        long themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        createReservation("브라운", "2026-05-05", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2026-05-05");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(409)
                .body("exceptionCode", equalTo("CANNOT_WAIT_OWN_RESERVATION"));
    }

    @Test
    void 대기_목록을_조회한다() {
        long timeId = createTime("10:00");
        long themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        createReservation("브라운", "2026-05-05", timeId, themeId);
        createWaiting("조이", "2026-05-05", timeId, themeId);
        createWaiting("러키", "2026-05-05", timeId, themeId);

        RestAssured.given().log().all()
                .queryParam("name", "조이")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", equalTo("조이"))
                .body("[0].rank", equalTo(1));
    }

    @Test
    void 대기를_삭제한다() {
        long timeId = createTime("10:00");
        long themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        createReservation("브라운", "2026-05-05", timeId, themeId);
        long waitingId = createWaiting("조이", "2026-05-05", timeId, themeId);

        RestAssured.given().log().all()
                .queryParam("name", "조이")
                .when().delete("/waitings/" + waitingId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .queryParam("name", "조이")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("$", empty());
    }

    private long createTime(String startAt) {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private long createTheme(String name, String description, String thumbnail) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", description);
        params.put("thumbnail", thumbnail);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private long createReservation(String name, String date, long timeId, long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private long createWaiting(String name, String date, long timeId, long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getLong("id");
    }
}
