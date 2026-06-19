package roomescape.reservation.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.PaymentGatewayException;
import roomescape.reservation.application.port.out.payment.PaymentConfirmation;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.application.port.out.payment.PaymentStatus;
import tools.jackson.databind.ObjectMapper;

class TossClientRetryAfterTest {

    private MockWebServer mockWebServer;
    private TossPaymentGateway tossPaymentGateway;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        tossPaymentGateway = new TossPaymentGateway(
                new TossClientConfig().tossRestClient(
                        mockWebServer.url("/").toString(),
                        "test_secret_key",
                        500,
                        500,
                        2
                ),
                new ObjectMapper()
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DisplayName("토스가 429와 Retry-After를 응답하면 기다린 뒤 같은 승인 요청을 재시도한다.")
    @Test
    void confirm_retries_after_toss_rate_limit_response() throws InterruptedException {
        mockWebServer.enqueue(tooManyRequests());
        mockWebServer.enqueue(success());

        PaymentResult result = tossPaymentGateway.confirm(paymentConfirmation());

        RecordedRequest firstRequest = mockWebServer.takeRequest();
        RecordedRequest secondRequest = mockWebServer.takeRequest();
        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
        assertThat(firstRequest.getHeader("Idempotency-Key")).isEqualTo("order-id");
        assertThat(secondRequest.getHeader("Idempotency-Key")).isEqualTo("order-id");
    }

    @DisplayName("토스 429가 반복되면 Retry-After 설정 횟수까지만 요청하고 기존 승인 재시도와 곱하지 않는다.")
    @Test
    void confirm_does_not_multiply_retry_after_attempts_by_gateway_retry() {
        mockWebServer.enqueue(tooManyRequests());
        mockWebServer.enqueue(tooManyRequests());

        assertThatThrownBy(() -> tossPaymentGateway.confirm(paymentConfirmation()))
                .isInstanceOf(PaymentGatewayException.class)
                .hasMessage("결제 승인에 실패했습니다.");

        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    private MockResponse tooManyRequests() {
        return new MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "0")
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "code": "TOO_MANY_REQUESTS",
                          "message": "요청량이 많습니다."
                        }
                        """);
    }

    private MockResponse success() {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "order-id",
                          "status": "DONE",
                          "totalAmount": 1000
                        }
                        """);
    }

    private PaymentConfirmation paymentConfirmation() {
        return new PaymentConfirmation("payment-key", "order-id", 1_000L);
    }
}
