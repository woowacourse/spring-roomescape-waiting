package roomescape.client;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.PaymentConfirmation;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TossPaymentGatewayOutboundRateLimitTest {
    private static final MockWebServer mockWebServer = new MockWebServer();

    static {
        try {
            mockWebServer.start();
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody("""
                                    {
                                      "paymentKey": "payment-key",
                                      "orderId": "order-1",
                                      "status": "DONE",
                                      "totalAmount": 10000
                                    }
                                    """);
                }
            });
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
        registry.add("toss.max-attempts", () -> "3");
        registry.add("toss.retry-after-default-seconds", () -> "0");
        registry.add("outbound-rate-limit.capacity", () -> "1");
        registry.add("outbound-rate-limit.refill-per-second", () -> "0.001");
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 설정된_아웃바운드_한도를_넘으면_토스로_보내지_않고_거부한다() {
        int beforeRequestCount = mockWebServer.getRequestCount();

        tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-1", 10_000L, "idempotency-key")
        );

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-2", 10_000L, "idempotency-key-2")
        )).isInstanceOf(PaymentGatewayException.OutboundRateLimited.class);

        assertThat(mockWebServer.getRequestCount() - beforeRequestCount).isEqualTo(1);
    }
}
