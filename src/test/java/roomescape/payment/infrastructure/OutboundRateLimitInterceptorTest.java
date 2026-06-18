package roomescape.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.function.LongSupplier;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import roomescape.global.ratelimit.TokenBucketRateLimiter;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.exception.OutboundRateLimitExceededException;

class OutboundRateLimitInterceptorTest {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private static MockWebServer mockWebServer;

    private final long[] clock = {0L};

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private LongSupplier fakeClock() {
        return () -> clock[0];
    }

    private void advanceSeconds(double seconds) {
        clock[0] += (long) (seconds * NANOS_PER_SECOND);
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("test_pk_1", "order_1", 10000L, "idem-key-1");
    }

    private TossPaymentGateway gatewayWithOutboundLimiter(TokenBucketRateLimiter limiter) {
        RestClient restClient = RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpf")
                .requestInterceptors(interceptors -> interceptors.add(new OutboundRateLimitInterceptor(limiter)))
                .build();
        return new TossPaymentGateway(restClient, new ObjectMapper());
    }

    private void enqueueSuccess() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {"paymentKey": "test_pk_1", "orderId": "order_1", "status": "DONE", "totalAmount": 10000}
                        """));
    }

    @Test
    void 한도를_초과한_호출은_서버로_보내지지_않고_거부된다() {
        long before = mockWebServer.getRequestCount();
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, fakeClock());
        TossPaymentGateway gateway = gatewayWithOutboundLimiter(limiter);
        enqueueSuccess();
        enqueueSuccess();

        var result = gateway.confirm(confirmation());
        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);

        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(OutboundRateLimitExceededException.class);

        assertThat(mockWebServer.getRequestCount() - before).isEqualTo(1);
    }

    @Test
    void 토큰이_보충되면_거부됐던_호출도_다시_나갈_수_있다() {
        long before = mockWebServer.getRequestCount();
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, fakeClock());
        TossPaymentGateway gateway = gatewayWithOutboundLimiter(limiter);
        enqueueSuccess();
        enqueueSuccess();

        gateway.confirm(confirmation());
        assertThatThrownBy(() -> gateway.confirm(confirmation()))
                .isInstanceOf(OutboundRateLimitExceededException.class);

        advanceSeconds(1);
        var result = gateway.confirm(confirmation());

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(mockWebServer.getRequestCount() - before).isEqualTo(2);
    }
}
