package roomescape.reservation.infra.toss;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.global.exception.PaymentAlreadyProcessedException;
import roomescape.global.exception.PaymentCardRejectedException;
import roomescape.global.exception.PaymentGatewayConfigurationException;
import roomescape.global.exception.PaymentGatewayException;
import roomescape.global.exception.PaymentInvalidRequestException;
import roomescape.global.exception.PaymentNotFoundException;
import roomescape.global.exception.RetryablePaymentGatewayException;
import roomescape.reservation.application.port.out.payment.PaymentConfirmation;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.application.port.out.payment.PaymentStatus;

class TossPaymentGatewayTest {

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private MockRestServiceServer server;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("https://api.tosspayments.com");
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        gateway = new TossPaymentGateway(restClientBuilder.build(), new ObjectMapper());
    }

    @AfterEach
    void verifyServer() {
        server.verify();
    }

    @DisplayName("토스 결제 승인 성공 응답을 결제 결과로 변환합니다.")
    @Test
    void confirm_maps_success_response_to_payment_result() {
        server.expect(once(), requestTo(TOSS_CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "paymentKey": "payment-key",
                                  "orderId": "order-id",
                                  "status": "DONE",
                                  "totalAmount": 1000
                                }
                                """));

        PaymentResult result = gateway.confirm(paymentConfirmation());

        assertSoftly(softly -> {
            softly.assertThat(result.paymentKey()).isEqualTo("payment-key");
            softly.assertThat(result.orderId()).isEqualTo("order-id");
            softly.assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
            softly.assertThat(result.approvedAmount()).isEqualTo(1_000L);
        });
    }

    @DisplayName("토스 결제 승인 오류 코드를 애플리케이션 결제 예외로 변환합니다.")
    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("tossErrorMappings")
    void confirm_maps_toss_error_code_to_payment_exception(
            String tossCode,
            Class<? extends RuntimeException> expectedException,
            String expectedMessage
    ) {
        server.expect(once(), requestTo(TOSS_CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "code": "%s",
                                  "message": "토스 원문 메시지"
                                }
                                """.formatted(tossCode)));

        assertThatThrownBy(() -> gateway.confirm(paymentConfirmation()))
                .isInstanceOf(expectedException)
                .hasMessage(expectedMessage);
    }

    @DisplayName("토스 결제 승인 오류 코드가 알 수 없는 값이면 일반 결제 게이트웨이 예외로 변환합니다.")
    @ParameterizedTest
    @MethodSource("unknownErrorBodies")
    void confirm_maps_unknown_or_malformed_toss_error_to_payment_gateway_exception(String responseBody) {
        server.expect(once(), requestTo(TOSS_CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody));

        assertThatThrownBy(() -> gateway.confirm(paymentConfirmation()))
                .isInstanceOf(PaymentGatewayException.class)
                .hasMessage("결제 승인에 실패했습니다.");
    }

    @DisplayName("토스 내부 처리 실패는 재시도 가능한 결제 게이트웨이 예외로 변환합니다.")
    @ParameterizedTest
    @MethodSource("retryableErrorBodies")
    void confirm_maps_retryable_toss_error_to_retryable_payment_gateway_exception(String responseBody) {
        server.expect(once(), requestTo(TOSS_CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody));

        assertThatThrownBy(() -> gateway.confirm(paymentConfirmation()))
                .isInstanceOf(RetryablePaymentGatewayException.class)
                .hasMessage("결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.");
    }

    private static Stream<Arguments> tossErrorMappings() {
        return Stream.of(
                Arguments.of(
                        "ALREADY_PROCESSED_PAYMENT",
                        PaymentAlreadyProcessedException.class,
                        "이미 승인된 결제입니다."
                ),
                Arguments.of(
                        "DUPLICATED_ORDER_ID",
                        PaymentInvalidRequestException.class,
                        "결제 요청이 올바르지 않거나 만료되었습니다. 다시 결제를 시도해주세요."
                ),
                Arguments.of(
                        "NOT_FOUND_PAYMENT_SESSION",
                        PaymentInvalidRequestException.class,
                        "결제 요청이 올바르지 않거나 만료되었습니다. 다시 결제를 시도해주세요."
                ),
                Arguments.of(
                        "INVALID_REQUEST",
                        PaymentInvalidRequestException.class,
                        "결제 요청이 올바르지 않거나 만료되었습니다. 다시 결제를 시도해주세요."
                ),
                Arguments.of(
                        "UNAUTHORIZED_KEY",
                        PaymentGatewayConfigurationException.class,
                        "결제 설정에 문제가 발생했습니다. 관리자에게 문의해주세요."
                ),
                Arguments.of(
                        "INVALID_API_KEY",
                        PaymentGatewayConfigurationException.class,
                        "결제 설정에 문제가 발생했습니다. 관리자에게 문의해주세요."
                ),
                Arguments.of(
                        "REJECT_CARD_PAYMENT",
                        PaymentCardRejectedException.class,
                        "카드 결제가 거절되었습니다. 다른 결제 수단으로 다시 시도해주세요."
                ),
                Arguments.of(
                        "NOT_FOUND_PAYMENT",
                        PaymentNotFoundException.class,
                        "결제 정보를 찾을 수 없습니다. 다시 결제를 시도해주세요."
                )
        );
    }

    private static Stream<String> unknownErrorBodies() {
        return Stream.of(
                """
                        {
                          "code": "NEW_TOSS_ERROR",
                          "message": "새 토스 오류"
                        }
                        """,
                """
                        {
                          "message": "코드가 없는 토스 오류"
                        }
                        """,
                "null",
                "{"
        );
    }

    private static Stream<String> retryableErrorBodies() {
        return Stream.of("""
                {
                  "code": "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
                  "message": "토스 내부 처리 실패"
                }
                """);
    }

    private PaymentConfirmation paymentConfirmation() {
        return new PaymentConfirmation("payment-key", "order-id", 1_000L);
    }
}
