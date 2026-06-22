package roomescape.adapter.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;

@SpringBootTest
class TossPaymentGatewayRetryTest {

    static MockWebServer mockWebServer;

    static {
        mockWebServer = new MockWebServer();
        try {
            mockWebServer.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> mockWebServer.url("/").toString());
        registry.add("toss.secret-key", () -> "test_sk_dummy");
        registry.add("toss.client-key", () -> "test_ck_dummy");
        registry.add("toss.connect-timeout-ms", () -> "2000");
        registry.add("toss.read-timeout-ms", () -> "5000");
        registry.add("toss.max-attempts", () -> "3");
        registry.add("outbound-rate-limit.capacity", () -> "100");
        registry.add("outbound-rate-limit.refill-per-second", () -> "100");
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 토스가_429와_RetryAfter를_주면_대기후_재시도해_최종_성공한다() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"paymentKey\":\"pk\",\"orderId\":\"order-1\",\"status\":\"DONE\",\"totalAmount\":10000}"));

        PaymentResult result = tossPaymentGateway.confirm(new PaymentConfirmation("pk", "order-1", 10000L));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }
}
