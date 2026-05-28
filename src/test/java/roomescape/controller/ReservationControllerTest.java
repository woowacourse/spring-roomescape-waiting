package roomescape.controller;

import static org.hamcrest.Matchers.equalTo;

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
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("사용자 예약 삭제")
    @Test
    void 사용자_예약_삭제() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 1);
        params.put("themeId", 1);

        long id = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .when().delete("/reservations/{id}", id)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("사용자 예약 조회")
    @Test
    void 사용자_예약_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("username", "김철수")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("존재하지 않는 시간으로 예약하면 404")
    @Test
    void 존재하지_않는_시간으로_예약하면_400() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 999);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 예약 시간입니다."));
    }

    @DisplayName("존재하지 않는 테마로 예약하면 404")
    @Test
    void 존재하지_않는_테마로_예약하면_400() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 1);
        params.put("themeId", 999);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 테마입니다."));
    }

    @DisplayName("이미 예약된 시간이면 409")
    @Test
    void 이미_예약된_시간이면_409() {
        // data.sql 첫 번째 예약: 2026-04-29, time_id=3, theme_id=1
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2026-04-29");
        params.put("timeId", 3);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("이미 예약된 시간입니다."));
    }

    @DisplayName("과거 날짜로 예약하면 422")
    @Test
    void 과거_날짜로_예약하면_422() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().minusDays(1).toString());
        params.put("timeId", 2);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422)
                .body("message", equalTo("과거 날짜로는 예약할 수 없습니다."));
    }

    @DisplayName("예약 변경 성공")
    @Test
    void 예약_변경_성공() {
        Map<String, Object> createParams = new HashMap<>();
        createParams.put("name", "브라운");
        createParams.put("date", LocalDate.now().plusDays(1).toString());
        createParams.put("timeId", 1);
        createParams.put("themeId", 1);

        long id = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(createParams)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("date", LocalDate.now().plusDays(2).toString());
        updateParams.put("timeId", 2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(updateParams)
                .when().patch("/reservations/{id}", id)
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("존재하지 않는 예약 변경하면 404")
    @Test
    void 존재하지_않는_예약_변경하면_404() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", 999)
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 예약입니다."));
    }

    @DisplayName("과거 날짜로 예약 변경하면 422")
    @Test
    void 과거_날짜로_예약_변경하면_422() {
        Map<String, Object> createParams = new HashMap<>();
        createParams.put("name", "브라운");
        createParams.put("date", LocalDate.now().plusDays(1).toString());
        createParams.put("timeId", 1);
        createParams.put("themeId", 1);

        long id = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(createParams)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("date", LocalDate.now().minusDays(1).toString());
        updateParams.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(updateParams)
                .when().patch("/reservations/{id}", id)
                .then().log().all()
                .statusCode(422)
                .body("message", equalTo("과거 날짜로는 예약할 수 없습니다."));
    }

    @DisplayName("이미 예약된 시간으로 변경하면 409")
    @Test
    void 이미_예약된_시간으로_변경하면_409() {
        Map<String, Object> createParams = new HashMap<>();
        createParams.put("name", "브라운");
        createParams.put("date", LocalDate.now().plusDays(1).toString());
        createParams.put("timeId", 1);
        createParams.put("themeId", 1);

        long id = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(createParams)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // 같은 날짜·시간에 다른 예약 생성
        Map<String, Object> anotherParams = new HashMap<>();
        anotherParams.put("name", "포비");
        anotherParams.put("date", LocalDate.now().plusDays(1).toString());
        anotherParams.put("timeId", 2);
        anotherParams.put("themeId", 1);
        RestAssured.given().contentType(ContentType.JSON).body(anotherParams)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("date", LocalDate.now().plusDays(1).toString());
        updateParams.put("timeId", 2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(updateParams)
                .when().patch("/reservations/{id}", id)
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("이미 예약된 시간입니다."));
    }

    @DisplayName("빈 슬롯에 대기 신청하면 409")
    @Test
    void 빈_슬롯에_대기_신청하면_409() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("예약 가능한 시간입니다. 일반 예약 API를 이용해주세요."));
    }

    @DisplayName("이미 예약된 슬롯에 다른 사람이 대기 신청하면 201")
    @Test
    void 예약된_슬롯에_대기_신청_성공() {
        String futureDate = LocalDate.now().plusDays(2).toString();

        Map<String, Object> reservationParams = new HashMap<>();
        reservationParams.put("name", "김철수");
        reservationParams.put("date", futureDate);
        reservationParams.put("timeId", 4);
        reservationParams.put("themeId", 2);
        RestAssured.given().contentType(ContentType.JSON).body(reservationParams)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "이영희");
        params.put("date", futureDate);
        params.put("timeId", 4);
        params.put("themeId", 2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("본인이 이미 예약한 슬롯에 대기 신청하면 409")
    @Test
    void 본인_예약_슬롯에_대기_신청하면_409() {
        // data.sql: 김철수가 2026-05-10 time_id=3 theme_id=1 예약 보유
        Map<String, Object> params = new HashMap<>();
        params.put("name", "김철수");
        params.put("date", "2026-05-10");
        params.put("timeId", 3);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("이미 예약된 시간입니다."));
    }

    @DisplayName("이미 대기 신청한 슬롯에 또 신청하면 409")
    @Test
    void 중복_대기_신청하면_409() {
        String futureDate = LocalDate.now().plusDays(3).toString();

        Map<String, Object> reservationParams = new HashMap<>();
        reservationParams.put("name", "김철수");
        reservationParams.put("date", futureDate);
        reservationParams.put("timeId", 5);
        reservationParams.put("themeId", 3);
        RestAssured.given().contentType(ContentType.JSON).body(reservationParams)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "이영희");
        params.put("date", futureDate);
        params.put("timeId", 5);
        params.put("themeId", 3);

        RestAssured.given().contentType(ContentType.JSON).body(params)
                .when().post("/reservations/waiting")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("이미 대기 신청한 시간입니다."));
    }

    @DisplayName("존재하지 않는 시간으로 대기 신청하면 404")
    @Test
    void 존재하지_않는_시간으로_대기_신청하면_404() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "이영희");
        params.put("date", "2026-05-10");
        params.put("timeId", 999);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 예약 시간입니다."));
    }

    @DisplayName("존재하지 않는 테마로 대기 신청하면 404")
    @Test
    void 존재하지_않는_테마로_대기_신청하면_404() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "이영희");
        params.put("date", "2026-05-10");
        params.put("timeId", 3);
        params.put("themeId", 999);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 테마입니다."));
    }

    @DisplayName("대기 취소 성공")
    @Test
    void 대기_취소_성공() {
        String futureDate = LocalDate.now().plusDays(4).toString();

        Map<String, Object> reservationParams = new HashMap<>();
        reservationParams.put("name", "김철수");
        reservationParams.put("date", futureDate);
        reservationParams.put("timeId", 6);
        reservationParams.put("themeId", 4);
        RestAssured.given().contentType(ContentType.JSON).body(reservationParams)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "이영희");
        params.put("date", futureDate);
        params.put("timeId", 6);
        params.put("themeId", 4);

        long waitingId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .when().delete("/reservations/waiting/{id}", waitingId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("대기 목록 조회 성공")
    @Test
    void 대기_목록_조회() {
        RestAssured.given().log().all()
                .queryParam("username", "이영희")
                .when().get("/reservations/waiting")
                .then().log().all()
                .statusCode(200);
    }
}
