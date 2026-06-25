package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;
import roomescape.exception.ProblemType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

class ReservationApiTest extends AbstractE2eTest {

    @Test
    void 예약이_없으면_빈_목록을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0))
                .body("totalCount", is(0))
                .body("page", is(0))
                .body("size", is(20));
    }

    @Test
    void 예약을_추가하면_201과_생성된_예약을_반환한다() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-08-05");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("민욱"))
                .body("date", is("2026-08-05"));
    }

    @Test
    void 예약을_추가하면_PENDING으로_저장되고_주문정보를_반환한다() {
        Integer timeId = createTime("16:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-08-05");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        JsonPath response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("orderId", notNullValue())
                .body("amount", is(50000))
                .extract().jsonPath();

        Integer reservationId = response.get("id");
        String orderId = response.getString("orderId");

        String status = jdbcTemplate.queryForObject(
                "SELECT reservation_status FROM reservation WHERE id = ?", String.class, reservationId);
        assertThat(status).isEqualTo("PENDING");

        Integer paymentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment WHERE order_id = ? AND reservation_id = ?",
                Integer.class, orderId, reservationId);
        assertThat(paymentCount).isEqualTo(1);
    }

    @Test
    void 예약을_추가한_뒤_조회하면_목록에_포함된다() {
        Integer timeId = createTime("14:00");
        Integer themeId = createTheme("추리", "단서를 찾아라", "https://example.com/mystery.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "티뉴");
        params.put("date", "2026-09-01");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("totalCount", is(1))
                .body("reservations[0].name", is("티뉴"));
    }

    @Test
    void 예약을_추가한_뒤_삭제하면_목록에서_제거된다() {
        Integer timeId = createTime("18:00");
        Integer themeId = createTheme("SF", "우주에서 탈출", "https://example.com/sf.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2026-10-10");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        Integer reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0))
                .body("totalCount", is(0));
    }

    @Test
    void 페이지_크기보다_많은_예약이_있으면_size만큼만_반환된다() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        for (int i = 1; i <= 5; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "사용자" + i);
            params.put("date", "2026-08-0" + i);
            params.put("timeId", timeId);
            params.put("themeId", themeId);

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations");
        }

        RestAssured.given().log().all()
                .when().get("/reservations?page=0&size=3")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(3))
                .body("totalCount", is(5))
                .body("page", is(0))
                .body("size", is(3));
    }

    @Test
    void 두번째_페이지_조회시_나머지_예약이_반환된다() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        for (int i = 1; i <= 5; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "사용자" + i);
            params.put("date", "2026-08-0" + i);
            params.put("timeId", timeId);
            params.put("themeId", themeId);

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations");
        }

        RestAssured.given().log().all()
                .when().get("/reservations?page=1&size=3")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(2))
                .body("totalCount", is(5))
                .body("page", is(1))
                .body("size", is(3));
    }

    @Test
    void 존재하지_않는_날짜로_예약하면_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-02-31");
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
    void 윤년이_아닌_해의_2월_29일로_예약하면_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-02-29");
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
    void 빈_이름으로_예약하면_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("date", "2026-08-05");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("type", is(ProblemType.VALIDATION_ERROR.uri().toString()));
    }

    @Test
    void 이름이_누락된_요청으로_예약하면_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-08-05");
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
    void timeId가_누락된_요청으로_예약하면_400() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-08-05");
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
    void 같은_날짜_시간_테마로_중복_예약하면_409() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-08-05");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("type", is(ProblemType.CONFLICT.uri().toString()));
    }

    @Test
    void 지난_날짜로_예약하면_422() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2020-01-01");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    void 존재하지_않는_시간_ID로_예약하면_404() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-08-05");
        params.put("timeId", 9999);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 존재하지_않는_테마_ID로_예약하면_404() {
        Integer timeId = createTime("11:00");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-08-05");
        params.put("timeId", timeId);
        params.put("themeId", 9999);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 본인_예약_조회는_이름이_일치하는_예약만_반환한다() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        createReservation("민욱", "2026-08-05", timeId, themeId);
        Integer time2 = createTime("15:00");
        createReservation("티뉴", "2026-08-05", time2, themeId);

        RestAssured.given().log().all()
                .when().get("/reservations/me?name=민욱")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", is("민욱"));
    }

    @Test
    void 본인_예약_조회는_결제_상태와_주문_정보를_포함한다() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        createReservation("민욱", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .when().get("/reservations/me?name=민욱")
                .then().log().all()
                .statusCode(200)
                .body("reservations[0].paymentStatus", is("PENDING"))
                .body("reservations[0].orderId", notNullValue())
                .body("reservations[0].amount", is(50000))
                .body("reservations[0].paymentKey", nullValue());
    }

    @Test
    void 본인_예약이_없으면_빈_목록이_반환된다() {
        RestAssured.given().log().all()
                .when().get("/reservations/me?name=민욱")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0));
    }

    @Test
    void 이름_파라미터가_없으면_400() {
        RestAssured.given().log().all()
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 본인_예약_취소는_204를_반환한다() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation("민욱", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/reservations/me/" + reservationId + "?name=민욱")
                .then().log().all()
                .statusCode(204);

        RestAssured.given()
                .when().get("/reservations/me?name=민욱")
                .then()
                .statusCode(200)
                .body("reservations.size()", is(0));
    }

    @Test
    void 다른_사람_이름으로_취소하면_401() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation("민욱", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/reservations/me/" + reservationId + "?name=티뉴")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 존재하지_않는_예약을_취소하면_404() {
        RestAssured.given().log().all()
                .when().delete("/reservations/me/9999?name=민욱")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 존재하지_않는_예약을_관리자가_삭제하면_404() {
        RestAssured.given().log().all()
                .when().delete("/reservations/9999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 지난_예약을_취소하면_422() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Long reservationId = insertPastReservation("민욱", "2020-01-01", timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/reservations/me/" + reservationId + "?name=민욱")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    void 관리자가_지난_예약을_삭제하면_422() {
        Integer timeId = createTime("11:30");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Long reservationId = insertPastReservation("민욱", "2020-01-01", timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    void 취소_요청에_이름_파라미터가_없으면_400() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation("민욱", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 본인_예약_변경은_200을_반환한다() {
        Integer timeId = createTime("13:00");
        Integer newTimeId = createTime("15:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation("민욱", "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");
        body.put("timeId", newTimeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId + "?name=민욱")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 같은_슬롯으로_변경해도_충돌로_보지_않는다() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation("민욱", "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-08-05");
        body.put("timeId", timeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId + "?name=민욱")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 다른_사람_이름으로_변경하면_401() {
        Integer timeId = createTime("13:00");
        Integer newTimeId = createTime("15:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation("민욱", "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");
        body.put("timeId", newTimeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId + "?name=티뉴")
                .then().log().all()
                .statusCode(401)
                .body("type", is(ProblemType.UNAUTHORIZED.uri().toString()));
    }

    @Test
    void 존재하지_않는_예약을_변경하면_404() {
        Integer timeId = createTime("13:00");

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");
        body.put("timeId", timeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/9999?name=민욱")
                .then().log().all()
                .statusCode(404)
                .body("type", is(ProblemType.NOT_FOUND.uri().toString()));
    }

    @Test
    void 같은_날짜_테마에_다른_예약이_있는_시간으로_변경하면_409() {
        Integer timeId = createTime("13:00");
        Integer otherTimeId = createTime("15:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation("민욱", "2026-08-05", timeId, themeId);
        createReservation("티뉴", "2026-08-05", otherTimeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-08-05");
        body.put("timeId", otherTimeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId + "?name=민욱")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 지난_시각으로_변경하면_422() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation("민욱", "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2020-01-01");
        body.put("timeId", timeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId + "?name=민욱")
                .then().log().all()
                .statusCode(422)
                .body("type", is(ProblemType.BUSINESS_RULE_VIOLATION.uri().toString()));
    }

    @Test
    void 이미_지난_예약을_변경하면_422() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Long reservationId = insertPastReservation("민욱", "2020-01-01", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");
        body.put("timeId", timeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId + "?name=민욱")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    void 변경_요청에_이름_파라미터가_없으면_400() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation("민욱", "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");
        body.put("timeId", timeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(400)
                .body("type", is(ProblemType.BAD_REQUEST.uri().toString()));
    }

    @Test
    void 변경_요청에_timeId가_누락되면_400() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation("민욱", "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId + "?name=민욱")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 변경_요청의_새_시간_ID가_존재하지_않으면_404() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation("민욱", "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");
        body.put("timeId", 9999);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId + "?name=민욱")
                .then().log().all()
                .statusCode(404);
    }

    private Long insertPastReservation(String name, String date, Integer timeId, Integer themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, LocalDate.parse(date), timeId, themeId
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE name = ? AND date = ?",
                Long.class, name, LocalDate.parse(date)
        );
    }

}
