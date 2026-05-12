package roomescape;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeApiTest {

    @Test
    void 시간_조회_빈목록() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("reservationTimes.size()", is(0));
    }

    @Test
    void 시간_추가() {
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
    void 시간_추가_후_조회() {
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
    void 시간_추가_및_삭제() {
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
    void 가용_시간_조회_등록된_시간_없음() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        RestAssured.given().log().all()
                .when().get("/times/availability?date=2026-08-05&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(0));
    }

    @Test
    void 가용_시간_조회_예약_없으면_모두_false() {
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
    void 가용_시간_조회_일부_예약된_시간만_true() {
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
    void 가용_시간_조회_다른_테마의_예약은_영향_없음() {
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
    void 가용_시간_조회_다른_날짜의_예약은_영향_없음() {
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

    private Integer createTime(String startAt) {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");
    }

    private Integer createTheme(String name, String description, String thumbnailImageUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", description);
        params.put("thumbnailImageUrl", thumbnailImageUrl);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");
    }

    private void createReservation(String name, String date, Integer timeId, Integer themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }
}
