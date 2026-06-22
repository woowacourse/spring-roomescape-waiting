package roomescape.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationControllerTest extends ControllerTest {

    @DisplayName("사용자 예약 추가")
    @Test
    void 사용자_예약_추가_API() {
        String date = LocalDate.now().plusDays(1).toString();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("브라운", date, 1, 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("date", equalTo(date))
                .body("time", equalTo("10:00"))
                .body("themeName", equalTo("공포의 저택"))
                .body("reservationStatus", equalTo("CONFIRMED"));
    }

    @DisplayName("예약 결제 대기 주문 생성")
    @Test
    void 예약_결제_대기_주문_생성() {
        String date = LocalDate.now().plusDays(1).toString();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("브라운", date, 1, 1))
                .when().post("/reservations/payment")
                .then().log().all()
                .statusCode(201)
                .body("id", equalTo(1))
                .body("orderId", matchesPattern("[A-Za-z0-9_-]{6,64}"))
                .body("amount", equalTo(10000))
                .body("clientKey", equalTo("test_gck_test"))
                .body("orderName", equalTo("공포의 저택 예약"))
                .body("reservationStatus", equalTo("PAYMENT_PENDING"));
    }

    @DisplayName("결제 대기 중인 슬롯은 다시 결제 대기 주문을 만들 수 없다")
    @Test
    void 결제_대기_중인_슬롯이면_409() {
        String date = LocalDate.now().plusDays(1).toString();
        Map<String, Object> params = reservationParams("브라운", date, 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/payment")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/payment")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("이미 예약된 시간입니다."));
    }

    @DisplayName("사용자 예약 삭제")
    @Test
    void 사용자_예약_삭제() {
        long id = createReservation("브라운", LocalDate.now().plusDays(1).toString(), 1, 1);

        RestAssured.given().log().all()
                .queryParam("username", "브라운")
                .when().delete("/reservations/{id}", id)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("본인 예약이 아니면 삭제할 수 없다")
    @Test
    void 본인_예약이_아니면_삭제할_수_없다() {
        long id = createReservation("브라운", LocalDate.now().plusDays(1).toString(), 1, 1);

        RestAssured.given().log().all()
                .queryParam("username", "이든")
                .when().delete("/reservations/{id}", id)
                .then().log().all()
                .statusCode(403)
                .body("message", equalTo("본인의 예약 또는 대기만 관리할 수 있습니다."));
    }

    @DisplayName("존재하지 않는 예약 삭제하면 404")
    @Test
    void 존재하지_않는_예약_삭제하면_404() {
        RestAssured.given().log().all()
                .queryParam("username", "브라운")
                .when().delete("/reservations/{id}", 999)
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 예약입니다."));
    }

    @DisplayName("사용자 예약 조회")
    @Test
    void 사용자_예약_조회() {
        String date = LocalDate.now().plusDays(1).toString();
        createReservation("조회자", date, 1, 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("username", "조회자")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations[0].date", equalTo(date))
                .body("reservations[0].reservationStatus", equalTo("CONFIRMED"));
    }

    @DisplayName("존재하지 않는 시간으로 예약하면 404")
    @Test
    void 존재하지_않는_시간으로_예약하면_400() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("브라운", LocalDate.now().plusDays(1).toString(), 999, 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 예약 시간입니다."));
    }

    @DisplayName("존재하지 않는 테마로 예약하면 404")
    @Test
    void 존재하지_않는_테마로_예약하면_400() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("브라운", LocalDate.now().plusDays(1).toString(), 1, 999))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 테마입니다."));
    }

    @DisplayName("이미 예약된 시간이면 409")
    @Test
    void 이미_예약된_시간이면_409() {
        String date = LocalDate.now().plusDays(30).toString();
        createReservation("포비", date, 3, 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("브라운", date, 3, 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("이미 예약된 시간입니다."));
    }

    @DisplayName("과거 날짜로 예약하면 422")
    @Test
    void 과거_날짜로_예약하면_422() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("브라운", LocalDate.now().minusDays(1).toString(), 2, 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422)
                .body("message", equalTo("과거 날짜로는 예약할 수 없습니다."));
    }

    @DisplayName("예약 요청에 시간이 없으면 400")
    @Test
    void 예약_요청에_시간이_없으면_400() {
        assertBadRequestWhenCreateReservation(
                reservationParamsWithout("timeId"),
                "timeId 널이어서는 안됩니다"
        );
    }

    @DisplayName("예약 요청에 이름이 없으면 400")
    @Test
    void 예약_요청에_이름이_없으면_400() {
        assertBadRequestWhenCreateReservation(
                reservationParamsWithout("name"),
                "name 공백일 수 없습니다"
        );
    }

    @DisplayName("예약 요청에 날짜가 없으면 400")
    @Test
    void 예약_요청에_날짜가_없으면_400() {
        assertBadRequestWhenCreateReservation(
                reservationParamsWithout("date"),
                "date 널이어서는 안됩니다"
        );
    }

    @DisplayName("예약 요청에 테마가 없으면 400")
    @Test
    void 예약_요청에_테마가_없으면_400() {
        assertBadRequestWhenCreateReservation(
                reservationParamsWithout("themeId"),
                "themeId 널이어서는 안됩니다"
        );
    }

    @DisplayName("예약 변경 성공")
    @Test
    void 예약_변경_성공() {
        long id = createReservation("브라운", LocalDate.now().plusDays(1).toString(), 1, 1);
        String updateDate = LocalDate.now().plusDays(2).toString();

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("date", updateDate);
        updateParams.put("timeId", 2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("username", "브라운")
                .body(updateParams)
                .when().patch("/reservations/{id}", id)
                .then().log().all()
                .statusCode(200)
                .body("date", equalTo(updateDate))
                .body("time", equalTo("11:00"))
                .body("reservationStatus", equalTo("CONFIRMED"));
    }

    @DisplayName("존재하지 않는 예약 변경하면 404")
    @Test
    void 존재하지_않는_예약_변경하면_404() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("username", "브라운")
                .body(params)
                .when().patch("/reservations/{id}", 999)
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 예약입니다."));
    }

    @DisplayName("과거 날짜로 예약 변경하면 422")
    @Test
    void 과거_날짜로_예약_변경하면_422() {
        long id = createReservation("브라운", LocalDate.now().plusDays(1).toString(), 1, 1);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("date", LocalDate.now().minusDays(1).toString());
        updateParams.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("username", "브라운")
                .body(updateParams)
                .when().patch("/reservations/{id}", id)
                .then().log().all()
                .statusCode(422)
                .body("message", equalTo("과거 날짜로는 예약할 수 없습니다."));
    }

    @DisplayName("이미 예약된 시간으로 변경하면 409")
    @Test
    void 이미_예약된_시간으로_변경하면_409() {
        long id = createReservation("브라운", LocalDate.now().plusDays(1).toString(), 1, 1);

        createReservation("포비", LocalDate.now().plusDays(1).toString(), 2, 1);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("date", LocalDate.now().plusDays(1).toString());
        updateParams.put("timeId", 2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("username", "브라운")
                .body(updateParams)
                .when().patch("/reservations/{id}", id)
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("이미 예약된 시간입니다."));
    }

    @DisplayName("같은 날짜와 시간으로 예약 변경하면 409")
    @Test
    void 같은_날짜와_시간으로_예약_변경하면_409() {
        String date = LocalDate.now().plusDays(1).toString();
        long id = createReservation("브라운", date, 1, 1);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("date", date);
        updateParams.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("username", "브라운")
                .body(updateParams)
                .when().patch("/reservations/{id}", id)
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("기존 예약과 변경할 예약이 동일한 날짜와 시간입니다."));
    }

    @DisplayName("본인 예약이 아니면 변경할 수 없다")
    @Test
    void 본인_예약이_아니면_변경할_수_없다() {
        long id = createReservation("브라운", LocalDate.now().plusDays(1).toString(), 1, 1);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("date", LocalDate.now().plusDays(2).toString());
        updateParams.put("timeId", 2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("username", "이든")
                .body(updateParams)
                .when().patch("/reservations/{id}", id)
                .then().log().all()
                .statusCode(403)
                .body("message", equalTo("본인의 예약 또는 대기만 관리할 수 있습니다."));
    }

    @DisplayName("빈 슬롯에 대기 신청하면 409")
    @Test
    void 빈_슬롯에_대기_신청하면_409() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("브라운", LocalDate.now().plusDays(1).toString(), 1, 1))
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("예약 가능한 시간입니다. 일반 예약 API를 이용해주세요."));
    }

    @DisplayName("이미 예약된 슬롯에 다른 사람이 대기 신청하면 201")
    @Test
    void 예약된_슬롯에_대기_신청_성공() {
        String futureDate = LocalDate.now().plusDays(2).toString();
        createReservation("김철수", futureDate, 4, 2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("이영희", futureDate, 4, 2))
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201)
                .body("date", equalTo(futureDate))
                .body("time", equalTo("13:00"))
                .body("themeName", equalTo("우주 정거장"))
                .body("reservationStatus", equalTo("WAITING"));
    }

    @DisplayName("본인이 이미 예약한 슬롯에 대기 신청하면 409")
    @Test
    void 본인_예약_슬롯에_대기_신청하면_409() {
        String futureDate = LocalDate.now().plusDays(30).toString();
        createReservation("김철수", futureDate, 3, 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("김철수", futureDate, 3, 1))
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("이미 예약된 시간입니다."));
    }

    @DisplayName("이미 대기 신청한 슬롯에 또 신청하면 409")
    @Test
    void 중복_대기_신청하면_409() {
        String futureDate = LocalDate.now().plusDays(3).toString();
        createReservation("김철수", futureDate, 5, 3);
        createReservationWaiting("이영희", futureDate, 5, 3);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("이영희", futureDate, 5, 3))
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("이미 대기 신청한 시간입니다."));
    }

    @DisplayName("존재하지 않는 시간으로 대기 신청하면 404")
    @Test
    void 존재하지_않는_시간으로_대기_신청하면_404() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("이영희", LocalDate.now().plusDays(1).toString(), 999, 1))
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 예약 시간입니다."));
    }

    @DisplayName("존재하지 않는 테마로 대기 신청하면 404")
    @Test
    void 존재하지_않는_테마로_대기_신청하면_404() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("이영희", LocalDate.now().plusDays(1).toString(), 3, 999))
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 테마입니다."));
    }

    @DisplayName("대기 요청에 시간이 없으면 400")
    @Test
    void 대기_요청에_시간이_없으면_400() {
        assertBadRequestWhenCreateReservationWaiting(
                reservationParamsWithout("timeId"),
                "timeId 널이어서는 안됩니다"
        );
    }

    @DisplayName("대기 요청에 이름이 없으면 400")
    @Test
    void 대기_요청에_이름이_없으면_400() {
        assertBadRequestWhenCreateReservationWaiting(
                reservationParamsWithout("name"),
                "name 공백일 수 없습니다"
        );
    }

    @DisplayName("대기 요청에 날짜가 없으면 400")
    @Test
    void 대기_요청에_날짜가_없으면_400() {
        assertBadRequestWhenCreateReservationWaiting(
                reservationParamsWithout("date"),
                "date 널이어서는 안됩니다"
        );
    }

    @DisplayName("대기 요청에 테마가 없으면 400")
    @Test
    void 대기_요청에_테마가_없으면_400() {
        assertBadRequestWhenCreateReservationWaiting(
                reservationParamsWithout("themeId"),
                "themeId 널이어서는 안됩니다"
        );
    }

    @DisplayName("대기 취소 성공")
    @Test
    void 대기_취소_성공() {
        String futureDate = LocalDate.now().plusDays(4).toString();
        createReservation("김철수", futureDate, 6, 4);
        long waitingId = createReservationWaiting("이영희", futureDate, 6, 4);

        RestAssured.given().log().all()
                .queryParam("username", "이영희")
                .when().delete("/reservations/waiting/{id}", waitingId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("본인 대기가 아니면 취소할 수 없다")
    @Test
    void 본인_대기가_아니면_취소할_수_없다() {
        String futureDate = LocalDate.now().plusDays(4).toString();
        createReservation("김철수", futureDate, 6, 4);
        long waitingId = createReservationWaiting("이영희", futureDate, 6, 4);

        RestAssured.given().log().all()
                .queryParam("username", "이든")
                .when().delete("/reservations/waiting/{id}", waitingId)
                .then().log().all()
                .statusCode(403)
                .body("message", equalTo("본인의 예약 또는 대기만 관리할 수 있습니다."));
    }

    @DisplayName("존재하지 않는 대기 취소하면 404")
    @Test
    void 존재하지_않는_대기_취소하면_404() {
        RestAssured.given().log().all()
                .queryParam("username", "이영희")
                .when().delete("/reservations/waiting/{id}", 999)
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 대기입니다."));
    }

    @DisplayName("대기 목록 조회 성공")
    @Test
    void 대기_목록_조회() {
        String futureDate = LocalDate.now().plusDays(4).toString();
        createReservation("김철수", futureDate, 6, 4);
        createReservationWaiting("이든", futureDate, 6, 4);

        RestAssured.given().log().all()
                .queryParam("username", "이든")
                .when().get("/reservations/waiting")
                .then().log().all()
                .statusCode(200)
                .body("reservations[0].date", equalTo(futureDate))
                .body("reservations[0].time", equalTo("15:00"))
                .body("reservations[0].themeName", equalTo("탐정 사무소"))
                .body("reservations[0].waitingNumber", equalTo(1))
                .body("reservations[0].reservationStatus", equalTo("WAITING"));
    }

    @DisplayName("예약과 대기 목록 통합 조회 성공")
    @Test
    void 예약과_대기_목록_조회() {
        String futureDate = LocalDate.now().plusDays(4).toString();
        createReservation("김철수", futureDate, 6, 4);
        createReservation("이든", futureDate, 7, 4);
        createReservationWaiting("이든", futureDate, 6, 4);

        RestAssured.given().log().all()
                .queryParam("username", "이든")
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("reservations[0].date", equalTo(futureDate))
                .body("reservations[0].time", equalTo("15:00"))
                .body("reservations[0].reservationStatus", equalTo("WAITING"))
                .body("reservations[0].waitingNumber", equalTo(1))
                .body("reservations[1].date", equalTo(futureDate))
                .body("reservations[1].time", equalTo("16:00"))
                .body("reservations[1].reservationStatus", equalTo("CONFIRMED"))
                .body("reservations[1].waitingNumber", nullValue());
    }

    private long createReservation(String name, String date, long timeId, long themeId) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservationParams(name, date, timeId, themeId))
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private long createReservationWaiting(String name, String date, long timeId, long themeId) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservationParams(name, date, timeId, themeId))
                .when().post("/reservations/waiting")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private void assertBadRequestWhenCreateReservation(Map<String, Object> params, String message) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo(message));
    }

    @DisplayName("내 예약 목록에서 결제 대기 주문의 결제 정보를 확인할 수 있다")
    @Test
    void 내_예약_목록_결제_정보_조회() {
        String date = LocalDate.now().plusDays(30).toString();
        String orderId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams("브라운", date, 1, 1))
                .when().post("/reservations/payment")
                .then().log().all()
                .statusCode(201)
                .extract().path("orderId");

        RestAssured.given().log().all()
                .queryParam("username", "브라운")
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", equalTo(1))
                .body("reservations[0].reservationStatus", equalTo("PAYMENT_PENDING"))
                .body("reservations[0].paymentStatus", equalTo("PENDING"))
                .body("reservations[0].orderId", equalTo(orderId))
                .body("reservations[0].amount", equalTo(10000));
    }

    private void assertBadRequestWhenCreateReservationWaiting(Map<String, Object> params, String message) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo(message));
    }

    private Map<String, Object> reservationParamsWithout(String field) {
        Map<String, Object> params = reservationParams("브라운", LocalDate.now().plusDays(1).toString(), 1, 1);
        params.remove(field);
        return params;
    }

    private Map<String, Object> reservationParams(String name, String date, long timeId, long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);
        return params;
    }
}
