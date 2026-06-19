package roomescape.reservation.infra.toss;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.global.exception.PaymentCardRejectedException;
import roomescape.global.exception.RetryablePaymentGatewayException;
import roomescape.reservation.application.port.out.payment.PaymentConfirmation;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.application.port.out.payment.PaymentStatus;
import tools.jackson.databind.ObjectMapper;

class TossPaymentGatewayRetryTest {

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

    @DisplayName("토스 결제 승인에서 재시도 가능한 오류 후 성공하면 결제 결과를 반환합니다.")
    @Test
    void confirm_retries_retryable_toss_error_and_returns_payment_result_when_second_attempt_succeeds() {
        // Given: 같은 주문번호의 멱등키로 첫 요청은 토스 내부 처리 실패, 두 번째 요청은 성공하도록 준비합니다.
        expectRetryableTossError();
        expectSuccess();

        // When: 결제 승인을 한 번 요청합니다.
        PaymentResult result = gateway.confirm(paymentConfirmation());

        // Then: 어댑터 내부에서 한 번 재시도되어 최종 성공 결과가 반환됩니다.
        assertSoftly(softly -> {
            softly.assertThat(result.paymentKey()).isEqualTo("payment-key");
            softly.assertThat(result.orderId()).isEqualTo("order-id");
            softly.assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
            softly.assertThat(result.approvedAmount()).isEqualTo(1_000L);
        });
    }

    @DisplayName("토스 결제 승인에서 재시도 가능한 오류가 반복되면 두 번만 요청하고 예외를 던집니다.")
    @Test
    void confirm_retries_retryable_toss_error_once_and_throws_when_second_attempt_fails() {
        // Given: 같은 주문번호의 멱등키로 두 번 모두 재시도 가능한 토스 오류가 응답되도록 준비합니다.
        expectRetryableTossError();
        expectRetryableTossError();

        // When & Then: 결제 승인은 최초 요청과 한 번의 자동 재시도 후 재시도 가능 예외로 종료됩니다.
        assertThatThrownBy(() -> gateway.confirm(paymentConfirmation()))
                .isInstanceOf(RetryablePaymentGatewayException.class)
                .hasMessage("결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.");
    }

    @DisplayName("토스 결제 승인에서 재시도 불가능한 오류는 재시도하지 않고 바로 예외를 던집니다.")
    @Test
    void confirm_does_not_retry_non_retryable_toss_error() {
        // Given: 카드 거절처럼 재시도로 해결되지 않는 토스 오류가 응답되도록 준비합니다.
        server.expect(once(), requestTo(TOSS_CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Idempotency-Key", "order-id"))
                .andRespond(withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "code": "REJECT_CARD_PAYMENT",
                                  "message": "카드 결제가 거절되었습니다."
                                }
                                """));

        // When & Then: 재시도 없이 카드 거절 예외로 변환됩니다.
        assertThatThrownBy(() -> gateway.confirm(paymentConfirmation()))
                .isInstanceOf(PaymentCardRejectedException.class)
                .hasMessage("카드 결제가 거절되었습니다. 다른 결제 수단으로 다시 시도해주세요.");
    }

    private void expectRetryableTossError() {
        server.expect(once(), requestTo(TOSS_CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Idempotency-Key", "order-id"))
                .andRespond(withServerError()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "code": "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
                                  "message": "토스 내부 처리 실패"
                                }
                                """));
    }

    private void expectSuccess() {
        server.expect(once(), requestTo(TOSS_CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Idempotency-Key", "order-id"))
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
    }

    private PaymentConfirmation paymentConfirmation() {
        return new PaymentConfirmation("payment-key", "order-id", 1_000L);
    }
}
