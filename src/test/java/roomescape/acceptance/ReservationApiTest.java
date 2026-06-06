package roomescape.acceptance;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

class ReservationApiTest extends AcceptanceTest {

    public static final String FUTURE_FIRST_DATE = LocalDate.now().plusDays(1).toString();
    public static final String FUTURE_SECOND_DATE = LocalDate.now().plusDays(2).toString();

    @Test
    void 예약_조회_빈목록() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @Sql("/data_relative_dates.sql")
    void 자신의_이름으로_예약_목록을_조회() {
        String findName = "김민수";
        RestAssured.given().log().all()
                .when().get("/reservations?name=" + findName)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }

    @Test
    void 예약_추가_후_조회() {
        Integer timeId = createTime("14:00");
        Integer themeId = createTheme("추리", "단서를 찾아라", "https://example.com/mystery.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "티뉴");
        params.put("date", FUTURE_FIRST_DATE);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("티뉴"))
                .body("date", is(FUTURE_FIRST_DATE));

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("티뉴"));
    }

    @Test
    void 예약_추가_및_삭제() {
        Integer timeId = createTime("18:00");
        Integer themeId = createTheme("SF", "우주에서 탈출", "https://example.com/sf.jpg");

        Integer reservationId = createReservation("브라운", FUTURE_FIRST_DATE, timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 예약_추가할_때_과거_시점인_경우_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        String pastDate = LocalDate.now().minusDays(1).toString();

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", pastDate);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 없는_예약을_삭제할_수_없다_404() {
        RestAssured.given().log().all()
                .when().delete("/reservations/" + 1)
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 내_예약을_취소한다() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        String name = "브라운";
        String waitlistName = "브리";

        Integer reservationId = createReservation(name, FUTURE_FIRST_DATE, timeId, themeId);
        Integer waitlistId = createReservation(waitlistName, FUTURE_FIRST_DATE, timeId, themeId);

        RestAssured.given().log().all()
                .queryParam("name", name)
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations?name=" + name)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));

        RestAssured.given().log().all()
                .when().get("/reservations?name=" + waitlistName)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .queryParam("name", waitlistName)
                .when().delete("/waitlists/" + waitlistId)
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 존재하지_않는_예약을_수정하거나_취소하면_404() {
        Integer updateTimeId = createTime("12:00");

        Map<String, Object> params = new HashMap<>();
        params.put("date", FUTURE_SECOND_DATE);
        params.put("timeId", updateTimeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "브라운")
                .body(params)
                .when().patch("/reservations/" + 1)
                .then().log().all()
                .statusCode(404);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/reservations/" + 1)
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 내_예약을_취소할_때_사용자_이름이_일치하지_않으면_403() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        String reservationOwner = "브라운";
        Integer reservationId = createReservation(reservationOwner, FUTURE_FIRST_DATE, timeId, themeId);

        RestAssured.given().log().all()
                .queryParam("name", "브리")
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(403);

        RestAssured.given().log().all()
                .when().get("/reservations?name=" + reservationOwner)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @Sql("/data_relative_dates.sql")
    void 내_예약을_취소할_때_이미_지난_예약이면_400() {
        RestAssured.given().log().all()
                .queryParam("name", "김민수")
                .when().delete("/reservations/" + 1)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 잘못된_요청은_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("date", FUTURE_FIRST_DATE);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 범위를_벗어난_월로_예약하면_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-13-01");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약을_수정한다() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        String name = "브라운";
        String waitlistName = "브리";
        Integer reservationId = createReservation(name, FUTURE_FIRST_DATE, timeId, themeId);
        Integer waitlistId = createReservation(waitlistName, FUTURE_FIRST_DATE, timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        Integer updateTimeId = createTime("12:00");
        params.put("date", FUTURE_SECOND_DATE);
        params.put("timeId", updateTimeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", name)
                .body(params)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(200)
                .body("id", is(reservationId))
                .body("name", is(name))
                .body("date", is(FUTURE_SECOND_DATE))
                .body("time.id", is(updateTimeId))
                .body("time.startAt", is("12:00"))
                .body("theme.id", is(themeId));

        RestAssured.given().log().all()
                .when().get("/reservations?name=" + waitlistName)
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is(waitlistName))
                .body("[0].date", is(FUTURE_FIRST_DATE))
                .body("[0].time.id", is(timeId))
                .body("[0].theme.id", is(themeId));

        RestAssured.given().log().all()
                .queryParam("name", waitlistName)
                .when().delete("/waitlists/" + waitlistId)
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약을_수정할_때_사용자_이름이_일치하지_않으면_403() {
        Integer timeId = createTime("10:00");
        Integer updateTimeId = createTime("12:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Integer reservationId = createReservation("브라운", FUTURE_FIRST_DATE, timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("date", FUTURE_SECOND_DATE);
        params.put("timeId", updateTimeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "브리")
                .body(params)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @Sql("/data_relative_dates.sql")
    void 예약을_수정할_때_이미_지난_예약이면_400() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", FUTURE_SECOND_DATE);
        params.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "김민수")
                .body(params)
                .when().patch("/reservations/" + 1)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약을_수정할_때_변경하려는_날짜와_시간이_과거이면_400() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        String name = "브라운";
        Integer reservationId = createReservation(name, FUTURE_FIRST_DATE, timeId, themeId);

        String pastDate = LocalDate.now().minusDays(1).toString();

        Map<String, Object> params = new HashMap<>();
        params.put("date", pastDate);
        params.put("timeId", timeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", name)
                .body(params)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약을_수정할_때_변경하려는_예약_시간이_이미_차_있으면_409() {
        Integer tenClockId = createTime("10:00");
        Integer twelveClockId = createTime("12:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        String name = "브라운";
        Integer reservationId = createReservation(name, FUTURE_FIRST_DATE, tenClockId, themeId);

        createReservation("브리", FUTURE_SECOND_DATE, twelveClockId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("date", FUTURE_SECOND_DATE);
        params.put("timeId", twelveClockId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", name)
                .body(params)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(409);
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

    private Integer createReservation(String name, String date, Integer timeId, Integer themeId) {
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
                .extract().jsonPath().get("id");
    }
}
