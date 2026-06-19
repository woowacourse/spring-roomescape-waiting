package roomescape.payment;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.payment.application.port.out.PaymentGateway;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.support.ControllerTestSupport;

public class PaymentApiIntegrationTest extends ControllerTestSupport {

    @MockitoBean
    private PaymentGateway paymentGateway;

    @Test
    @DisplayName("예약 생성은 결제 대기 주문을 만들고 승인 성공 시 예약을 확정한다.")
    void creates_pending_reservation_and_confirms_payment() {
        String accessToken = loginUserToken();
        Map<String, Object> order = createReservationOrder(accessToken);
        String orderId = (String) order.get("orderId");
        Integer amount = (Integer) order.get("amount");
        Integer reservationId = (Integer) order.get("id");
        when(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .thenReturn(new PaymentResult("payment-key", orderId, "DONE", amount));

        RestAssured.given().log().all()
                .header("Authorization", bearer(accessToken))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "paymentKey", "payment-key",
                        "orderId", orderId,
                        "amount", amount
                ))
                .when().post("/api/user/payments/confirm")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.status", is("CONFIRMED"))
                .body("data.paymentKey", is("payment-key"));

        RestAssured.given().log().all()
                .header("Authorization", bearer(accessToken))
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("data.find { it.id == " + reservationId + " }.status", is("CONFIRMED"));
    }

    @Test
    @DisplayName("조작된 결제 금액은 승인 호출 전에 차단한다.")
    void amount_mismatch_is_blocked_before_gateway_call() {
        String accessToken = loginUserToken();
        Map<String, Object> order = createReservationOrder(accessToken);

        RestAssured.given().log().all()
                .header("Authorization", bearer(accessToken))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "paymentKey", "payment-key",
                        "orderId", order.get("orderId"),
                        "amount", ((Integer) order.get("amount")) + 1
                ))
                .when().post("/api/user/payments/confirm")
                .then().log().all()
                .statusCode(400)
                .body("success", is(false))
                .body("error.code", is("PAYMENT_400_AMOUNT_MISMATCH"));

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    @DisplayName("다른 사용자의 결제 대기 주문은 승인할 수 없다.")
    void other_member_cannot_confirm_payment() {
        String ownerToken = loginUserToken();
        String otherToken = loginWaitingUserToken();
        Map<String, Object> order = createReservationOrder(ownerToken);

        RestAssured.given().log().all()
                .header("Authorization", bearer(otherToken))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "paymentKey", "payment-key",
                        "orderId", order.get("orderId"),
                        "amount", order.get("amount")
                ))
                .when().post("/api/user/payments/confirm")
                .then().log().all()
                .statusCode(403)
                .body("success", is(false))
                .body("error.code", is("RESERVATION_403_OWNER"));

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    @DisplayName("다른 사용자의 결제 실패 콜백은 대기 주문을 삭제하지 않는다.")
    void other_member_cannot_delete_pending_payment_on_failure() {
        String ownerToken = loginUserToken();
        String otherToken = loginWaitingUserToken();
        Map<String, Object> order = createReservationOrder(ownerToken);

        RestAssured.given().log().all()
                .header("Authorization", bearer(otherToken))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "code", "PAY_PROCESS_ABORTED",
                        "message", "실패",
                        "orderId", order.get("orderId")
                ))
                .when().post("/api/user/payments/fail")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .header("Authorization", bearer(ownerToken))
                .when().get("/api/user/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("data.find { it.id == " + order.get("id") + " }.status", is("PENDING"));
    }

    @Test
    @DisplayName("failUrl에서 orderId가 없어도 안전하게 처리한다.")
    void handles_fail_url_without_order_id() {
        String accessToken = loginUserToken();

        RestAssured.given().log().all()
                .header("Authorization", bearer(accessToken))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "code", "PAY_PROCESS_CANCELED",
                        "message", "사용자가 결제를 취소했습니다."
                ))
                .when().post("/api/user/payments/fail")
                .then().log().all()
                .statusCode(204);
    }

    private Map<String, Object> createReservationOrder(String accessToken) {
        return RestAssured.given().log().all()
                .header("Authorization", bearer(accessToken))
                .contentType(ContentType.JSON)
                .body(reservationRequest())
                .when().post("/api/user/reservations")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.status", is("PENDING"))
                .body("data.orderId", notNullValue())
                .body("data.amount", is(10000))
                .extract()
                .path("data");
    }

    private Map<String, Object> reservationRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2026-05-05");
        reservation.put("timeId", 4);
        reservation.put("themeId", 4);
        return reservation;
    }
}
