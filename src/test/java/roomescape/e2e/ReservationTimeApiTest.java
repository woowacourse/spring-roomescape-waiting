package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class ReservationTimeApiTest extends AbstractE2eTest {

    @Test
    void 시간이_없으면_빈_목록을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("reservationTimes.size()", is(0));
    }

    @Test
    void 시간을_추가하면_201과_생성된_시간을_반환한다() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("startAt", is("10:00"));
    }

    @Test
    void 시간을_추가한_뒤_조회하면_목록에_포함된다() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "13:30");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("reservationTimes.size()", is(1))
                .body("reservationTimes[0].startAt", is("13:30"));
    }

    @Test
    void 시간을_추가한_뒤_삭제하면_목록에서_제거된다() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "20:00");

        Integer id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");

        RestAssured.given().log().all()
                .when().delete("/times/" + id)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("reservationTimes.size()", is(0));
    }

    @Test
    void 사용중인_시간을_삭제하면_422() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer timeId = createTime("10:00");
        createReservation("브라운", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/times/" + timeId)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    void 잘못된_시간_형식으로_추가하면_400() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "25:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void startAt이_누락된_요청으로_시간_추가하면_400() {
        Map<String, String> params = new HashMap<>();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 가용_시간_조회시_등록된_시간이_없으면_빈_목록을_반환한다() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        RestAssured.given().log().all()
                .when().get("/times/availability?date=2026-08-05&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(0));
    }

    @Test
    void 가용_시간_조회시_예약이_없으면_모든_시간이_false를_반환한다() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        createTime("10:00");
        createTime("11:00");

        RestAssured.given().log().all()
                .when().get("/times/availability?date=2026-08-05&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(2))
                .body("times.reserved", contains(false, false));
    }

    @Test
    void 가용_시간_조회시_예약된_시간만_true를_반환한다() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservedTimeId = createTime("10:00");
        createTime("11:00");
        createReservation("브라운", "2026-08-05", reservedTimeId, themeId);

        RestAssured.given().log().all()
                .when().get("/times/availability?date=2026-08-05&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(2))
                .body("times.find { it.id == " + reservedTimeId + " }.reserved", is(true))
                .body("times.findAll { it.id != " + reservedTimeId + " }.reserved", hasItems(false));
    }

    @Test
    void 가용_시간_조회시_다른_테마의_예약은_영향을_주지_않는다() {
        Integer themeA = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer themeB = createTheme("추리", "단서를 찾아라", "https://example.com/mystery.jpg");
        Integer timeId = createTime("10:00");
        createReservation("브라운", "2026-08-05", timeId, themeB);

        RestAssured.given().log().all()
                .when().get("/times/availability?date=2026-08-05&themeId=" + themeA)
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(1))
                .body("times[0].reserved", is(false));
    }

    @Test
    void 가용_시간_조회시_다른_날짜의_예약은_영향을_주지_않는다() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer timeId = createTime("10:00");
        createReservation("브라운", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .when().get("/times/availability?date=2026-08-06&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(1))
                .body("times[0].reserved", is(false));
    }

}
