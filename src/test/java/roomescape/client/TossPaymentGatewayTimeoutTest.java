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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TossPaymentGatewayTimeoutTest {

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
    void 응답이_느리면_read_timeout만큼만_기다렸다가_실패한다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "paymentKey": "payment-key",
                          "orderId": "order-1",
                          "status": "DONE",
                          "totalAmount": 10000
                        }
                        """)
                .setHeadersDelay(2, TimeUnit.SECONDS));

        long start = System.nanoTime();

        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("payment-key", "order-1", 10_000L, "idempotency-key")
        ))
                .isInstanceOf(PaymentGatewayException.ReadTimeout.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertThat(elapsedMs).isLessThan(1_500L);
    }
}
