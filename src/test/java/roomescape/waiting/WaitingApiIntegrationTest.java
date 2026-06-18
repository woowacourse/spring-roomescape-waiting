package roomescape.waiting;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.ControllerTestSupport;

public class WaitingApiIntegrationTest extends ControllerTestSupport {

    @Test
    @DisplayName("이미 예약된 슬롯에 대기를 신청할 수 있다.")
    void creates_waiting_for_reserved_slot_successfully() {
        String accessToken = loginWaitingUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", notNullValue())
                .body("data.memberId", is(2))
                .body("data.slotId", is(1))
                .body("data.waitingOrder", is(1));
    }

    @Test
    @DisplayName("미션 원문 경로로도 이미 예약된 슬롯에 대기를 신청할 수 있다.")
    void creates_waiting_with_mission_path_successfully() {
        String accessToken = loginWaitingUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.memberId", is(2))
                .body("data.slotId", is(1))
                .body("data.waitingOrder", is(1));
    }

    private Map<String, Object> waitingRequest() {
        return waitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L);
    }

    private Map<String, Object> waitingRequest(LocalDate date, long timeId, long themeId) {
        Map<String, Object> request = new HashMap<>();
        request.put("date", date);
        request.put("timeId", timeId);
        request.put("themeId", themeId);
        return request;
    }

    @Test
    @DisplayName("같은 슬롯에 대기를 신청하면 신청 순서대로 순번이 부여된다.")
    void assigns_waiting_order_by_request_sequence_for_same_slot() {
        String accessToken = loginWaitingUserToken();
        String otherAccessToken = loginOtherUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.memberId", is(2))
                .body("data.slotId", is(1))
                .body("data.waitingOrder", is(1));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + otherAccessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.memberId", is(3))
                .body("data.slotId", is(1))
                .body("data.waitingOrder", is(2));
    }

    @Test
    @DisplayName("해당 슬롯에 예약/대기가 모두 없으면 대기 신청에 실패한다.")
    void empty_slot_waiting_request_fails() {
        String accessToken = loginWaitingUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest(LocalDate.of(2026, 5, 5), 4L, 4L))
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(400)
                .body("success", is(false))
                .body("error.code", is("WAITING_400_TARGET_BAD_REQUEST"));
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복 대기를 신청할 수 없다.")
    void same_member_cannot_create_duplicate_waiting_for_same_slot() {
        String accessToken = loginWaitingUserToken();
        createWaiting(accessToken);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(409)
                .body("success", is(false))
                .body("error.code", is("WAITING_409"));
    }

    private Integer createWaiting(String accessToken) {
        return RestAssured.given().log().all()
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
    }

    @Test
    @DisplayName("본인이 이미 예약한 슬롯에는 대기를 신청할 수 없다.")
    void member_cannot_wait_for_own_reserved_slot() {
        String accessToken = loginUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest())
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(409)
                .body("success", is(false))
                .body("error.code", is("WAITING_409_OWN_RESERVATION"));
    }

    @Test
    @DisplayName("대기를 취소할 수 있다.")
    void cancels_waiting_successfully() {
        String accessToken = loginWaitingUserToken();
        Integer waitingId = createWaiting(accessToken);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .pathParam("id", waitingId)
                .when().delete("/api/user/waitings/{id}")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("대기를 취소하면 목록에서 사라지고 남은 대기 순번이 재계산된다.")
    void canceling_waiting_removes_it_and_reorders_remaining_waitings() {
        String firstAccessToken = loginWaitingUserToken();
        String secondAccessToken = loginOtherUserToken();

        Integer firstWaitingId = createWaiting(firstAccessToken);
        createWaiting(secondAccessToken);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + firstAccessToken)
                .contentType(ContentType.JSON)
                .pathParam("id", firstWaitingId)
                .when().delete("/api/user/waitings/{id}")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + firstAccessToken)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(0));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + secondAccessToken)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(1))
                .body("data[0].status", is("WAITING"))
                .body("data[0].waitingOrder", is(1));
    }

    @Test
    @DisplayName("매니저는 대기 목록을 조회할 수 있다.")
    void manager_finds_waiting_list_successfully() {
        createWaiting(loginWaitingUserToken());
        createWaiting(loginOtherUserToken());
        String managerAccessToken = loginManagerToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + managerAccessToken)
                .when().get("/api/manager/waitings")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(2))
                .body("data[0].waitingOrder", is(1))
                .body("data[1].waitingOrder", is(2));
    }

    @Test
    @DisplayName("매니저는 대기를 취소할 수 있다.")
    void manager_cancels_waiting_successfully() {
        String waitingUserToken = loginWaitingUserToken();
        Integer waitingId = createWaiting(waitingUserToken);
        String managerAccessToken = loginManagerToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + managerAccessToken)
                .contentType(ContentType.JSON)
                .pathParam("id", waitingId)
                .when().delete("/api/manager/waitings/{id}")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + waitingUserToken)
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(0));
    }

    @Test
    @DisplayName("없는 대기를 취소하면 404를 응답한다.")
    void canceling_missing_waiting_returns_not_found() {
        String accessToken = loginWaitingUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .pathParam("id", 999)
                .when().delete("/api/user/waitings/{id}")
                .then().log().all()
                .statusCode(404)
                .body("success", is(false))
                .body("error.code", is("WAITING_404"));
    }

    @Test
    @DisplayName("양수가 아닌 대기 id로 취소를 요청하면 400을 응답한다.")
    void non_positive_waiting_id_cancel_request_returns_bad_request() {
        String accessToken = loginWaitingUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .pathParam("id", -1)
                .when().delete("/api/user/waitings/{id}")
                .then().log().all()
                .statusCode(400)
                .body("success", is(false))
                .body("error.code", is("INVALID_INPUT_400"));
    }

    @Test
    @DisplayName("다른 사용자의 대기는 취소할 수 없다.")
    void member_cannot_cancel_other_members_waiting() {
        String accessToken = loginWaitingUserToken();
        String otherAccessToken = loginOtherUserToken();
        Integer waitingId = createWaiting(accessToken);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + otherAccessToken)
                .contentType(ContentType.JSON)
                .pathParam("id", waitingId)
                .when().delete("/api/user/waitings/{id}")
                .then().log().all()
                .statusCode(403)
                .body("success", is(false))
                .body("error.code", is("WAITING_403_OWNER"));
    }

}
