package roomescape.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;

class PaymentControllerTest extends ControllerTest {

    @MockitoBean
    PaymentGateway paymentGateway;

    @DisplayName("결제 성공 콜백은 결제를 승인하고 예약을 확정한다")
    @Test
    void 결제_성공_콜백_예약_확정() {
        String date = LocalDate.now().plusDays(1).toString();
        String orderId = createReservationPaymentOrder("브라운", date, 1, 1);
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult("payment_key", orderId, "DONE", 10000L));

        RestAssured.given().log().all()
                .queryParam("paymentKey", "payment_key")
                .queryParam("orderId", orderId)
                .queryParam("amount", 10000)
                .when().get("/payments/success")
                .then().log().all()
                .statusCode(200)
                .body("date", equalTo(date))
                .body("time", equalTo("10:00"))
                .body("themeName", equalTo("공포의 저택"))
                .body("reservationStatus", equalTo("RESERVED"));
    }

    @DisplayName("결제 성공 콜백의 amount가 저장 금액과 다르면 400")
    @Test
    void 결제_성공_콜백_amount_불일치() {
        String date = LocalDate.now().plusDays(1).toString();
        String orderId = createReservationPaymentOrder("브라운", date, 1, 1);

        RestAssured.given().log().all()
                .queryParam("paymentKey", "payment_key")
                .queryParam("orderId", orderId)
                .queryParam("amount", 9000)
                .when().get("/payments/success")
                .then().log().all()
                .statusCode(400);
    }

    private String createReservationPaymentOrder(String name, String date, long timeId, long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/payment")
                .then().log().all()
                .statusCode(201)
                .extract().path("orderId");
    }
}
