package roomescape.infrastructure.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.infrastructure.ratelimiter.TokenBucket;

@SpringBootTest
class TossClientTimeoutTest {

    private static final String BLACKHOLE_URL = "http://10.255.255.1:81";
    private static final String SUCCESS_BODY = """
            {
              "paymentKey": "payment_key",
              "orderId": "order_123456",
              "totalAmount": 37000
            }
            """;

    private static final MockWebServer mockWebServer = startMockWebServer();

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.toss.base-url", () -> mockWebServer.url("/").toString());
        registry.add("payment.toss.secret-key", () -> "test_gsk_dummy");
        registry.add("payment.toss.connect-timeout-ms", () -> "500");
        registry.add("payment.toss.read-timeout-ms", () -> "500");
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void readTimeout_보다_응답이_느리면_승인_결과_불명확_예외로_실패한다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY)
                .setHeadersDelay(2, TimeUnit.SECONDS));

        long start = System.nanoTime();
        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation()))
                .isInstanceOf(TossPaymentException.ConfirmationUnknown.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class)
                .extracting("code")
                .isEqualTo("TOSS_CONFIRMATION_UNKNOWN");
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(elapsedMs).isLessThan(1_500);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    void connectTimeout_보다_연결이_느리면_연결_실패_예외로_실패한다() {
        TossPaymentGateway gateway = new TossPaymentGateway(
                new TossClientConfig().tossRestClient(
                        BLACKHOLE_URL,
                        "test_gsk_dummy",
                        500,
                        500,
                        0,
                        new TokenBucket(1_000, 1_000, System::nanoTime)),
                new ObjectMapper());

        long start = System.nanoTime();
        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(TossPaymentException.ConnectionFailed.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class)
                .extracting("code")
                .isEqualTo("TOSS_CONNECTION_FAILED");
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(elapsedMs).isBetween(300L, 2_500L);
    }

    private static MockWebServer startMockWebServer() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return server;
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("payment_key", "order_123456", 37_000L, "idempotency-key-123");
    }
}
