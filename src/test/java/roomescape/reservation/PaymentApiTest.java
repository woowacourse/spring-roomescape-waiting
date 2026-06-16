package roomescape.reservation;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.global.exception.PaymentGatewayException;
import roomescape.reservation.application.port.out.payment.PaymentGateway;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.application.port.out.payment.PaymentStatus;
import roomescape.reservation.application.service.PaymentService;
import roomescape.reservation.domain.PaymentOrder;
import roomescape.support.ApiTest;
import roomescape.support.TestDataHelper;

@ApiTest
class PaymentApiTest {

    private static final String PAYMENT_KEY = "test_payment_key";

    @Autowired
    private TestDataHelper testHelper;

    @Autowired
    private PaymentService paymentService;

    @Value("${toss.client-key}")
    private String tossClientKey;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @DisplayName("결제 설정 조회 API는 공개 가능한 클라이언트 키만 반환합니다.")
    @Test
    void get_payment_config() {
        RestAssured.given()
                .when().get("/payments/config")
                .then().log().all()
                .statusCode(200)
                .body("clientKey", equalTo(tossClientKey))
                .body("$", not(hasKey("secretKey")));
    }

    @DisplayName("결제 승인 성공 API를 테스트합니다.")
    @Test
    void confirm_payment() {
        PaymentOrder order = preparePaymentOrder();
        given(paymentGateway.confirm(any()))
                .willReturn(
                        new PaymentResult(PAYMENT_KEY, order.getOrderId().value(), PaymentStatus.DONE,
                                order.getAmount().value()));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentConfirmRequest(order))
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(200)
                .body("paymentKey", equalTo(PAYMENT_KEY))
                .body("orderId", equalTo(order.getOrderId().value()))
                .body("status", equalTo("DONE"))
                .body("approvedAmount", equalTo(order.getAmount().value().intValue()));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findReservationStatus(order.getReservationId())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentOrderStatus(order.getOrderId().value())).isEqualTo("CONFIRMED");
        });
    }

    @DisplayName("결제 승인 결과가 DONE이 아니면 API는 결제 주문과 예약 상태를 확정하지 않습니다.")
    @Test
    void confirm_payment_non_done_result() {
        PaymentOrder order = preparePaymentOrder();
        given(paymentGateway.confirm(any()))
                .willReturn(
                        new PaymentResult(PAYMENT_KEY, order.getOrderId().value(), PaymentStatus.ABORTED,
                                order.getAmount().value()));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentConfirmRequest(order))
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(422);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findReservationStatus(order.getReservationId())).isEqualTo("PAYMENT_PENDING");
            softly.assertThat(testHelper.findPaymentOrderStatus(order.getOrderId().value())).isEqualTo("PENDING");
        });
    }

    @DisplayName("이미 확정된 결제 주문 승인 요청 시 API는 게이트웨이를 호출하지 않고 409를 반환합니다.")
    @Test
    void confirm_payment_already_confirmed_order() {
        PaymentOrder order = preparePaymentOrder();
        String savedPaymentKey = "already_confirmed_payment_key";
        testHelper.confirmPaymentOrder(order, savedPaymentKey);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentConfirmRequest(order))
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(409);

        verify(paymentGateway, never()).confirm(any());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findReservationStatus(order.getReservationId())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentOrderStatus(order.getOrderId().value())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentKey(order.getOrderId().value())).isEqualTo(savedPaymentKey);
        });
    }

    @DisplayName("결제 게이트웨이 장애가 발생하면 API는 502를 반환하고 결제 주문과 예약 상태를 확정하지 않습니다.")
    @Test
    void confirm_payment_gateway_exception() {
        PaymentOrder order = preparePaymentOrder();
        given(paymentGateway.confirm(any()))
                .willThrow(new PaymentGatewayException("결제 승인에 실패했습니다."));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentConfirmRequest(order))
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(502);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findReservationStatus(order.getReservationId())).isEqualTo("PAYMENT_PENDING");
            softly.assertThat(testHelper.findPaymentOrderStatus(order.getOrderId().value())).isEqualTo("PENDING");
        });
    }

    private PaymentOrder preparePaymentOrder() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        return paymentService.prepare(reservationId);
    }

    private Map<String, Object> paymentConfirmRequest(PaymentOrder order) {
        return Map.of(
                "paymentKey", PAYMENT_KEY,
                "orderId", order.getOrderId().value(),
                "amount", order.getAmount().value()
        );
    }
}
