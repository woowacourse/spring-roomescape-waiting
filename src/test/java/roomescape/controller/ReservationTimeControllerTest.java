package roomescape.controller;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeControllerTest extends ControllerTest {

    @DisplayName("API - 예약 시간 등록")
    @Test
    void API_예약_시간_등록() {
        String createStartAt = "23:00";

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTimeParams(createStartAt))
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", equalTo(createStartAt));
    }

    @DisplayName("API - 예약 시간 조회")
    @Test
    void API_예약_시간_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("times", org.hamcrest.Matchers.notNullValue());
    }

    @DisplayName("API - 예약 시간 삭제")
    @Test
    void API_예약_시간_삭제() {
        long id = createReservationTime("23:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/times/" + id)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("times", org.hamcrest.Matchers.notNullValue());
    }

    @DisplayName("존재하지 않는 예약 시간 삭제하면 404")
    @Test
    void 존재하지_않는_예약_시간_삭제하면_404() {
        RestAssured.given().log().all()
                .when().delete("/times/999")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 예약 시간입니다."));
    }

    @DisplayName("이미 존재하는 시간이면 409")
    @Test
    void 이미_존재하는_시간이면_400() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTimeParams("10:00"))
                .when().post("/times")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("이미 존재하는 예약 시간입니다."));
    }

    @DisplayName("예약 또는 대기에 사용 중인 시간 삭제하면 409")
    @Test
    void 예약에_사용중인_시간_삭제하면_400() {
        RestAssured.given().log().all()
                .when().delete("/times/3")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("예약 또는 대기에 사용 중인 시간은 삭제할 수 없습니다."));
    }

    private long createReservationTime(String startAt) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTimeParams(startAt))
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", equalTo(startAt))
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private Map<String, Object> reservationTimeParams(String startAt) {
        Map<String, Object> params = new HashMap<>();
        params.put("startAt", startAt);
        return params;
    }
}
