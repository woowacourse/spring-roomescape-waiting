package roomescape.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TossPaymentGatewayTest {

    private static final MockWebServer mockWebServer = new MockWebServer();

    static {
        try {
            mockWebServer.start();
        } catch (IOException exception) {
            throw new IllegalStateException("MockWebServer를 시작할 수 없습니다.", exception);
        }
    }

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> mockWebServer.url("/").toString());
        registry.add("toss.secret-key", () -> "test_sk_dummy");
        registry.add("toss.connect-timeout-ms", () -> "500");
        registry.add("toss.read-timeout-ms", () -> "500");
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 결제_승인에_성공하면_결제_결과를_반환한다() {
        enqueue(200, """
                {
                  "paymentKey": "payment-key",
                  "orderId": "order-1",
                  "status": "DONE",
                  "totalAmount": 10000
                }
                """);

        PaymentResult result = tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-1", 10_000L, "idempotency-key")
        );

        assertThat(result)
                .extracting(
                        PaymentResult::paymentKey,
                        PaymentResult::orderId,
                        PaymentResult::approvedAmount
                )
                .containsExactly("payment-key", "order-1", 10_000L);
    }

    @Test
    void 이미_처리된_결제면_AlreadyProcessed_예외가_발생한다() {
        enqueue(400, """
                {
                  "code": "ALREADY_PROCESSED_PAYMENT",
                  "message": "이미 처리된 결제 입니다."
                }
                """);

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-1", 10_000L, "idempotency-key")
        )).isInstanceOf(TossPaymentException.AlreadyProcessed.class);
    }

    @Test
    void 카드가_거절되면_CardRejected_예외가_발생한다() {
        enqueue(403, """
                {
                  "code": "REJECT_CARD_PAYMENT",
                  "message": "카드 결제가 거절되었습니다."
                }
                """);

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-1", 10_000L, "idempotency-key")
        )).isInstanceOf(TossPaymentException.CardRejected.class);
    }

    @Test
    void 인증키가_잘못되면_GatewayConfig_예외가_발생한다() {
        enqueue(401, """
                {
                  "code": "INVALID_API_KEY",
                  "message": "잘못된 시크릿 키입니다."
                }
                """);

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-1", 10_000L, "idempotency-key")
        )).isInstanceOf(TossPaymentException.GatewayConfig.class);
    }

    @Test
    void 토스_내부_오류면_Retryable_예외가_발생한다() {
        enqueue(500, """
                {
                  "code": "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
                  "message": "일시적인 오류가 발생했습니다."
                }
                """);

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-1", 10_000L, "idempotency-key")
        )).isInstanceOf(TossPaymentException.Retryable.class);
    }

    @Test
    void 정의되지_않은_에러코드는_기본_TossPaymentException이_발생한다() {
        enqueue(400, """
                {
                  "code": "UNKNOWN_ERROR",
                  "message": "정의되지 않은 에러입니다."
                }
                """);

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-1", 10_000L, "idempotency-key")
        ))
                .isInstanceOf(TossPaymentException.class)
                .isNotInstanceOf(TossPaymentException.AlreadyProcessed.class)
                .isNotInstanceOf(TossPaymentException.CardRejected.class)
                .isNotInstanceOf(TossPaymentException.GatewayConfig.class)
                .isNotInstanceOf(TossPaymentException.Retryable.class);
    }

    @Test
    void 결제_승인_API에_필요한_요청을_보낸다() throws Exception {
        enqueue(200, """
                {
                  "paymentKey": "payment-key",
                  "orderId": "order-1",
                  "status": "DONE",
                  "totalAmount": 10000
                }
                """);

        tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-1", 10_000L, "idempotency-key")
        );

        var request = mockWebServer.takeRequest();

        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/v1/payments/confirm");
        assertThat(request.getHeader("Content-Type")).contains("application/json");
        assertThat(request.getHeader("Authorization")).startsWith("Basic ");
        assertThat(request.getHeader("Idempotency-Key")).isEqualTo("idempotency-key");

        assertThat(request.getBody().readUtf8())
                .contains("\"paymentKey\":\"payment-key\"")
                .contains("\"orderId\":\"order-1\"")
                .contains("\"amount\":10000");
    }

    private void enqueue(int statusCode, String body) {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(statusCode)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }
}
