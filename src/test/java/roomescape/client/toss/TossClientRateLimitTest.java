package roomescape.client.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.client.PaymentConfirmation;
import roomescape.client.PaymentGatewayRetryableException;
import roomescape.client.PaymentStatus;
import roomescape.client.ratelimit.OutboundRateLimitException;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TossClientRateLimitTest {

    private static final String SUCCESS_BODY = """
            {"paymentKey": "test_pk_1", "orderId": "order-1", "status": "DONE", "totalAmount": 10000}
            """;
    private static final String ERROR_BODY = """
            {"code": "TOO_MANY_REQUESTS", "message": "too many requests"}
            """;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void Toss가_429와_Retry_After를_주면_재시도해_최종_성공한다() throws InterruptedException {
        mockWebServer.enqueue(tooManyRequests().setHeader("Retry-After", "0"));
        mockWebServer.enqueue(success());
        TossPaymentGateWay gateway = gateway(10, 10, 3, Duration.ofMillis(10));

        var result = gateway.confirm(confirmation());

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
        assertThat(mockWebServer.takeRequest().getHeader("Idempotency-Key")).isEqualTo("idempotency-key-1");
        assertThat(mockWebServer.takeRequest().getHeader("Idempotency-Key")).isEqualTo("idempotency-key-1");
    }

    @Test
    void Retry_After가_없으면_기본_고정_간격으로_재시도한다() {
        mockWebServer.enqueue(tooManyRequests());
        mockWebServer.enqueue(success());
        TossPaymentGateWay gateway = gateway(10, 10, 3, Duration.ofMillis(10));

        var result = gateway.confirm(confirmation());

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void maxAttempts를_넘어도_429이면_도메인_예외로_실패한다() {
        mockWebServer.enqueue(tooManyRequests());
        mockWebServer.enqueue(tooManyRequests());
        TossPaymentGateWay gateway = gateway(10, 10, 2, Duration.ZERO);

        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(PaymentGatewayRetryableException.class)
                .hasMessage("결제 API 호출 한도를 초과했습니다.");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void outbound_자체_한도를_넘으면_외부로_요청을_보내지_않는다() {
        mockWebServer.enqueue(success());
        TossPaymentGateWay gateway = gateway(1, 0.1, 3, Duration.ZERO);

        gateway.confirm(confirmation());

        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(OutboundRateLimitException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }

    private TossPaymentGateWay gateway(
            long outboundCapacity,
            double outboundRefillPerSec,
            int maxAttempts,
            Duration fallbackDelay
    ) {
        return new TossPaymentGateWay(
                new TossClientConfig().tossRestClient(
                        mockWebServer.url("/").toString(),
                        "test_gsk_dummy",
                        500,
                        500,
                        outboundCapacity,
                        outboundRefillPerSec,
                        maxAttempts,
                        fallbackDelay
                ),
                new ObjectMapper()
        );
    }

    private MockResponse success() {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY);
    }

    private MockResponse tooManyRequests() {
        return new MockResponse()
                .setResponseCode(429)
                .setHeader("Content-Type", "application/json")
                .setBody(ERROR_BODY);
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("test_pk_1", "order-1", 10_000L, "idempotency-key-1");
    }
}
