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
import java.net.SocketTimeoutException;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.global.exception.PaymentAlreadyProcessedException;
import roomescape.global.exception.PaymentCardRejectedException;
import roomescape.global.exception.PaymentGatewayConfigurationException;
import roomescape.global.exception.PaymentGatewayException;
import roomescape.global.exception.PaymentInvalidRequestException;
import roomescape.global.exception.PaymentNotFoundException;
import roomescape.global.exception.RetryablePaymentGatewayException;
import roomescape.reservation.application.port.out.payment.PaymentGateway;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.application.port.out.payment.PaymentStatus;
import roomescape.reservation.application.service.PaymentCommandService;
import roomescape.reservation.domain.Payment;
import roomescape.support.ApiTest;
import roomescape.support.TestDataHelper;

@ApiTest
class PaymentApiTest {

    private static final String PAYMENT_KEY = "test_payment_key";

    @Autowired
    private TestDataHelper testHelper;

    @Autowired
    private PaymentCommandService paymentCommandService;

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
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willReturn(
                        new PaymentResult(PAYMENT_KEY, payment.getOrderId().value(), PaymentStatus.DONE,
                                payment.getAmount().value()));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentConfirmRequest(payment))
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(200)
                .body("paymentKey", equalTo(PAYMENT_KEY))
                .body("orderId", equalTo(payment.getOrderId().value()))
                .body("status", equalTo("DONE"))
                .body("approvedAmount", equalTo(payment.getAmount().value().intValue()));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("CONFIRMED");
        });
    }

    @DisplayName("결제 승인 결과가 DONE이 아니면 API는 결제 주문과 예약 상태를 확정하지 않습니다.")
    @Test
    void confirm_payment_non_done_result() {
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willReturn(
                        new PaymentResult(PAYMENT_KEY, payment.getOrderId().value(), PaymentStatus.ABORTED,
                                payment.getAmount().value()));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentConfirmRequest(payment))
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(422);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("PAYMENT_PENDING");
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("PENDING");
        });
    }

    @DisplayName("이미 확정된 결제 주문 승인 요청 시 API는 게이트웨이를 호출하지 않고 409를 반환합니다.")
    @Test
    void confirm_payment_already_confirmed_order() {
        Payment payment = preparePayment();
        String savedPaymentKey = "already_confirmed_payment_key";
        testHelper.confirmPayment(payment, savedPaymentKey);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentConfirmRequest(payment))
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(409);

        verify(paymentGateway, never()).confirm(any());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentKey(payment.getOrderId().value())).isEqualTo(savedPaymentKey);
        });
    }

    @DisplayName("결제 게이트웨이 장애가 발생하면 API는 502를 반환하고 결제 주문과 예약 상태를 확정하지 않습니다.")
    @Test
    void confirm_payment_gateway_exception() {
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willThrow(new PaymentGatewayException("결제 승인에 실패했습니다."));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentConfirmRequest(payment))
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(502);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("PAYMENT_PENDING");
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("PENDING");
        });
    }

    @DisplayName("결제 승인 timeout이 발생하면 API는 503을 반환하고 결제 주문과 예약 상태를 확정하지 않습니다.")
    @Test
    void confirm_payment_timeout_preserves_pending_status() {
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willThrow(new RetryablePaymentGatewayException(new SocketTimeoutException()));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentConfirmRequest(payment))
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(503)
                .body("errorMessage", equalTo("결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요."))
                .body("$", not(hasKey("code")));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("PAYMENT_PENDING");
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("PENDING");
        });
    }

    @DisplayName("결제 승인 애플리케이션 예외는 API 응답 상태와 errorMessage 형식을 유지합니다.")
    @ParameterizedTest(name = "{1}")
    @MethodSource("paymentExceptionMappings")
    void confirm_payment_application_exception_response(
            RuntimeException exception,
            int expectedStatus,
            String expectedMessage
    ) {
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willThrow(exception);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentConfirmRequest(payment))
                .when().post("/payments/success")
                .then().log().all()
                .statusCode(expectedStatus)
                .body("errorMessage", equalTo(expectedMessage))
                .body("$", not(hasKey("code")));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("PAYMENT_PENDING");
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("PENDING");
        });
    }

    @DisplayName("결제 취소 실패 콜백에 주문 ID가 없으면 정리 없이 204를 반환합니다.")
    @Test
    void fail_payment_without_order_id_no_op() {
        Map<String, String> requestBody = Map.of(
                "code", "PAY_PROCESS_CANCELED",
                "message", "사용자가 결제를 취소했습니다."
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/payments/fail")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("결제 실패 콜백은 대기 중인 주문과 연결 예약을 정리하고 204를 반환합니다.")
    @Test
    void fail_payment_pending_order_cleanup() {
        Payment payment = preparePayment();

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentFailRequest(payment.getOrderId().value()))
                .when().post("/payments/fail")
                .then().log().all()
                .statusCode(204);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findOptionalPaymentStatus(payment.getOrderId().value())).isEmpty();
            softly.assertThat(testHelper.existsReservation(payment.getReservationId())).isFalse();
        });
    }

    @DisplayName("결제 실패 콜백은 알 수 없는 주문 ID에도 멱등하게 204를 반환합니다.")
    @Test
    void fail_payment_unknown_order_id_no_op() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentFailRequest("unknown-order-id"))
                .when().post("/payments/fail")
                .then().log().all()
                .statusCode(204);
    }

    private static Stream<Arguments> paymentExceptionMappings() {
        return Stream.of(
                Arguments.of(
                        new PaymentAlreadyProcessedException(),
                        409,
                        "이미 승인된 결제입니다."
                ),
                Arguments.of(
                        new PaymentInvalidRequestException(),
                        422,
                        "결제 요청이 올바르지 않거나 만료되었습니다. 다시 결제를 시도해주세요."
                ),
                Arguments.of(
                        new PaymentCardRejectedException(),
                        422,
                        "카드 결제가 거절되었습니다. 다른 결제 수단으로 다시 시도해주세요."
                ),
                Arguments.of(
                        new PaymentNotFoundException(),
                        404,
                        "결제 정보를 찾을 수 없습니다. 다시 결제를 시도해주세요."
                ),
                Arguments.of(
                        new RetryablePaymentGatewayException(),
                        503,
                        "결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요."
                ),
                Arguments.of(
                        new PaymentGatewayConfigurationException(),
                        500,
                        "결제 설정에 문제가 발생했습니다. 관리자에게 문의해주세요."
                ),
                Arguments.of(
                        new PaymentGatewayException("결제 승인에 실패했습니다."),
                        502,
                        "결제 승인에 실패했습니다."
                )
        );
    }

    private Payment preparePayment() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        return paymentCommandService.prepare(reservationId);
    }

    private Map<String, Object> paymentConfirmRequest(Payment payment) {
        return Map.of(
                "paymentKey", PAYMENT_KEY,
                "orderId", payment.getOrderId().value(),
                "amount", payment.getAmount().value()
        );
    }

    private Map<String, Object> paymentFailRequest(String orderId) {
        return Map.of(
                "code", "PAY_PROCESS_CANCELED",
                "message", "사용자가 결제를 취소했습니다.",
                "orderId", orderId
        );
    }
}
