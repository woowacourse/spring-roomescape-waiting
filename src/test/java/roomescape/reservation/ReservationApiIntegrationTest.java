package roomescape.reservation.domain;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import roomescape.support.ControllerTestSupport;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ReservationApiIntegrationTest extends ControllerTestSupport {

    private Map<String, Object> reservationRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2026-05-05");
        reservation.put("timeId", 4);
        reservation.put("themeId", 4);
        return reservation;
    }

    private Map<String, Object> waitingRequest() {
        Map<String, Object> waiting = new HashMap<>();
        waiting.put("date", "2026-05-05");
        waiting.put("timeId", 1);
        waiting.put("themeId", 1);
        return waiting;
    }

    @Test
    void 예약_생성() {
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

    @Test
    void 나의_특정_예약_삭제_및_나의_예약_목록_조회() {
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
    void 양수가_아닌_예약_id로_삭제를_요청하면_400을_응답한다() {
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
    void 나의_예약_목록에서_대기도_함께_조회한다() {
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

    @Test
    void 예약_취소_시_1번_대기가_자동_승격된다() {
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
                .body("data[0].status", is("RESERVED"));
    }

    @Test
    void 남은_대기_순번_재정렬_테스트() {
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
                .body("data[0].status", is("RESERVED"))
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
    void 매니저_예약_목록_조회() {
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
    void 매니저_예약_삭제() {
        String accessToken = loginManagerToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("id", 1)
                .when().delete("/api/manager/reservations/{id}")
                .then().log().all()
                .statusCode(204);
    }

}
