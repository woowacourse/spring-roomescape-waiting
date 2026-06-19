package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserReservationTest {

    @Test
    @DisplayName("이름으로 내 예약 목록을 조회한다.")
    void getMyReservations() {
        RestAssured.given().log().all()
                .queryParam("name", "user_a")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("user_a"));
    }

    @Test
    @DisplayName("존재하지 않는 이름으로 조회하면 빈 목록을 반환한다.")
    void getMyReservationsWithUnknownName() {
        RestAssured.given().log().all()
                .queryParam("name", "unknown")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("예약 요청 시 결제 대기 예약과 결제창 정보를 생성한다.")
    void createPendingReservationWithPaymentCheckout() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "new-user");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 2L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("new-user"))
                .body("date", is("2026-06-05"))
                .body("time.id", is(1))
                .body("theme.id", is(2))
                .body("status", is("PENDING_PAYMENT"))
                .body("payment.amount", is(5_000))
                .body("payment.orderName", is("예약없는테마 예약"));
    }

    @Test
    @DisplayName("결제 대기 예약은 내 예약 목록에 결제대기로 노출한다.")
    void pendingReservationIsShownInMyReservations() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "pending-user");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 2L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .queryParam("name", "pending-user")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("결제대기"));
    }

    @Test
    @DisplayName("결제 대기 예약의 결제창 정보를 다시 조회한다.")
    void getPendingReservationPaymentCheckout() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "pending-user");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 2L);

        int reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().path("reservationId");

        RestAssured.given().log().all()
                .queryParam("name", "pending-user")
                .when().get("/reservations/" + reservationId + "/payment")
                .then().log().all()
                .statusCode(200)
                .body("reservationId", is(reservationId))
                .body("status", is("PENDING_PAYMENT"))
                .body("payment.orderId", notNullValue())
                .body("payment.amount", is(5_000));
    }

    @Test
    @DisplayName("미래 예약을 취소한다.")
    void cancelFutureReservation() {
        RestAssured.given().log().all()
                .queryParam("name", "user_b")
                .when().delete("/reservations/2")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("지난 예약 취소 시 400을 반환한다.")
    void cancelPastReservation() {
        RestAssured.given().log().all()
                .queryParam("name", "user_a")
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(400)
                .body("message", is("이미 시작된 예약은 변경할 수 없습니다."));
    }

    @Test
    @DisplayName("예약의 날짜와 시간을 변경한다.")
    void updateReservation() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-07-01");
        params.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "user_b")
                .body(params)
                .when().patch("/reservations/2")
                .then().log().all()
                .statusCode(200)
                .body("date", is("2026-07-01"))
                .body("time.id", is(1));
    }

    @Test
    @DisplayName("지난 시간으로 변경 시 400을 반환한다.")
    void updateReservationToPastTime() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-04-01");
        params.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", "user_b")
                .body(params)
                .when().patch("/reservations/2")
                .then().log().all()
                .statusCode(400)
                .body("message", is("지나간 시간에는 예약할 수 없습니다."));
    }

    @Test
    @DisplayName("예약과 예약 대기가 같이 조회되는지 확인한다.")
    void getMyReservationsAndWaitingTest() {
        RestAssured.given().log().all()
                .queryParam("name", "user_b")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].status", is("예약대기"))
                .body("[0].rank", is(2))
                .body("[1].status", is("예약"));
    }

    @Test
    @DisplayName("예약 취소 후 대기 1번이 내 예약 목록에서 결제대기로 조회된다.")
    void cancelPromotesWaitingInMyReservations() {
        RestAssured.given().log().all()
                .queryParam("name", "user_c")
                .when().delete("/reservations/3")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .queryParam("name", "user_e")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("결제대기"))
                .body("[0].rank", nullValue());

        RestAssured.given().log().all()
                .queryParam("name", "user_b")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("find { it.status == '예약대기' }.rank", is(1));
    }

    @Test
    @DisplayName("예약 취소로 승격된 결제대기 예약의 결제창 정보를 조회한다.")
    void getPromotedWaitingPaymentCheckout() {
        RestAssured.given().log().all()
                .queryParam("name", "user_c")
                .when().delete("/reservations/3")
                .then().log().all()
                .statusCode(204);

        int reservationId = RestAssured.given().log().all()
                .queryParam("name", "user_e")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract().path("[0].id");

        RestAssured.given().log().all()
                .queryParam("name", "user_e")
                .when().get("/reservations/" + reservationId + "/payment")
                .then().log().all()
                .statusCode(200)
                .body("reservationId", is(reservationId))
                .body("status", is("PENDING_PAYMENT"))
                .body("payment.orderId", notNullValue())
                .body("payment.amount", is(5_000));
    }
}
