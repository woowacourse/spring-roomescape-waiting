package roomescape.reservation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.ControllerTestSupport;

public class ReservationApiIntegrationTest extends ControllerTestSupport {

    @Test
    @DisplayName("예약을 생성할 수 있다.")
    void creates_reservation_successfully() {
        String accessToken = loginUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(reservationRequest())
                .when().post("/api/user/reservations")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", is(5))
                .body("data.memberId", is(1))
                .body("data.slotId", is(4));
    }

    private Map<String, Object> reservationRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2026-05-05");
        reservation.put("timeId", 4);
        reservation.put("themeId", 4);
        return reservation;
    }

    @Test
    @DisplayName("나의 특정 예약을 삭제하고 나의 예약 목록을 조회할 수 있다.")
    void deletes_my_reservation_and_returns_my_reservation_list() {
        String accessToken = loginUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(4));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("id", 1)
                .when().delete("/api/user/reservations/{id}")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(3));
    }

    @Test
    @DisplayName("양수가 아닌 예약 id로 삭제를 요청하면 400을 응답한다.")
    void non_positive_reservation_id_delete_request_returns_bad_request() {
        String accessToken = loginUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("id", -1)
                .when().delete("/api/user/reservations/{id}")
                .then().log().all()
                .statusCode(400)
                .body("success", is(false))
                .body("error.code", is("INVALID_INPUT_400"));
    }

    @Test
    @DisplayName("나의 예약 목록에서 대기도 함께 조회한다.")
    void my_reservation_list_includes_waitings() {
        String accessToken = loginWaitingUserToken();

        Integer waitingId = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", notNullValue())
                .extract()
                .path("data.id");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].id", is(waitingId))
                .body("data[0].status", is("WAITING"))
                .body("data[0].waitingOrder", is(1));
    }

    private Map<String, Object> waitingRequest() {
        Map<String, Object> waiting = new HashMap<>();
        waiting.put("date", "2026-05-05");
        waiting.put("timeId", 1);
        waiting.put("themeId", 1);
        return waiting;
    }

    @Test
    @DisplayName("예약 취소 시 첫 번째 대기가 자동 승격된다.")
    void canceling_reservation_promotes_first_waiting_automatically() {
        String reservationUserToken = loginUserToken();
        String waitingUserToken = loginWaitingUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + waitingUserToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", notNullValue())
                .extract()
                .path("data.id");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + reservationUserToken)
                .pathParam("id", 1)
                .when().delete("/api/user/reservations/{id}")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + waitingUserToken)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("data.size()", is(1))
                .body("data[0].status", is("CONFIRMED"));
    }

    @Test
    @DisplayName("남은 대기 순번을 재정렬한다.")
    void reorders_remaining_waiting_positions() {
        String reservationUserToken = loginUserToken();
        String waitingUserToken1 = loginOtherUserToken();
        String waitingUserToken2 = loginWaitingUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + waitingUserToken1)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.waitingOrder", is(1));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + waitingUserToken2)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.waitingOrder", is(2));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + reservationUserToken)
                .pathParam("id", 1)
                .when().delete("/api/user/reservations/{id}")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .header("Authorization", bearer(waitingUserToken1))
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].status", is("CONFIRMED"))
                .body("data[0].waitingOrder", is((Object) null));

        RestAssured.given().log().all()
                .header("Authorization", bearer(waitingUserToken2))
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].status", is("WAITING"))
                .body("data[0].waitingOrder", is(1));
    }

    @Test
    @DisplayName("매니저는 예약 목록을 조회할 수 있다.")
    void manager_finds_reservation_list_successfully() {
        String accessToken = loginManagerToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/manager/reservations")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(4));
    }

    @Test
    @DisplayName("매니저는 예약을 삭제할 수 있다.")
    void manager_deletes_reservation_successfully() {
        String accessToken = loginManagerToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("id", 1)
                .when().delete("/api/manager/reservations/{id}")
                .then().log().all()
                .statusCode(204);
    }

}
