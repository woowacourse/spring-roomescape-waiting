package roomescape.payment.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.ratelimit.TokenBucketRateLimiter;

class TossRateLimitIntegrationTest {

    @Test
    void 토스_429의_Retry_After를_존중해_같은_멱등키로_재시도한다() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(429)
                    .setHeader("Retry-After", "0"));
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .setBody("""
                            {
                              "paymentKey": "payment-key",
                              "orderId": "ROOM_order123",
                              "totalAmount": 10000,
                              "status": "DONE"
                            }
                            """));
            server.start();

            TokenBucketRateLimiter outboundLimiter =
                    new TokenBucketRateLimiter(2, 1, () -> 0L);
            OutboundRateLimitInterceptor outboundInterceptor =
                    new OutboundRateLimitInterceptor(outboundLimiter);
            RestClient restClient = RestClient.builder()
                    .baseUrl(server.url("/").toString())
                    .requestInterceptor(outboundInterceptor)
                    .requestInterceptor(new RetryAfterInterceptor(
                            2,
                            Duration.ZERO,
                            outboundInterceptor::acquire
                    ))
                    .build();
            PaymentProperties properties = new PaymentProperties(
                    new PaymentProperties.Toss(
                            server.url("/").toString(),
                            "test-client-key",
                            "test-secret-key",
                            Duration.ofSeconds(1),
                            Duration.ofSeconds(1)
                    ),
                    10_000L
            );
            TossPaymentGateway gateway =
                    new TossPaymentGateway(restClient, new ObjectMapper(), properties);

            PaymentResult result = gateway.confirm(
                    new PaymentConfirmation("payment-key", "ROOM_order123", 10_000L),
                    "fixed-idempotency-key"
            );

            assertThat(result.status()).isEqualTo("DONE");
            RecordedRequest first = server.takeRequest(1, TimeUnit.SECONDS);
            RecordedRequest second = server.takeRequest(1, TimeUnit.SECONDS);
            assertThat(first).isNotNull();
            assertThat(second).isNotNull();
            assertThat(first.getHeader("Idempotency-Key"))
                    .isEqualTo("fixed-idempotency-key");
            assertThat(second.getHeader("Idempotency-Key"))
                    .isEqualTo("fixed-idempotency-key");
        }
    }

    @Test
    void 재시도도_outbound_한도를_넘으면_외부로_보내지_않는다() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(429)
                    .setHeader("Retry-After", "0"));
            server.start();

            TokenBucketRateLimiter outboundLimiter =
                    new TokenBucketRateLimiter(1, 1, () -> 0L);
            OutboundRateLimitInterceptor outboundInterceptor =
                    new OutboundRateLimitInterceptor(outboundLimiter);
            RestClient restClient = RestClient.builder()
                    .baseUrl(server.url("/").toString())
                    .requestInterceptor(outboundInterceptor)
                    .requestInterceptor(new RetryAfterInterceptor(
                            2,
                            Duration.ZERO,
                            outboundInterceptor::acquire
                    ))
                    .build();
            PaymentProperties properties = new PaymentProperties(
                    new PaymentProperties.Toss(
                            server.url("/").toString(),
                            "test-client-key",
                            "test-secret-key",
                            Duration.ofSeconds(1),
                            Duration.ofSeconds(1)
                    ),
                    10_000L
            );
            TossPaymentGateway gateway =
                    new TossPaymentGateway(restClient, new ObjectMapper(), properties);

            assertThatThrownBy(() -> gateway.confirm(
                    new PaymentConfirmation("payment-key", "ROOM_order123", 10_000L),
                    "fixed-idempotency-key"
            )).isInstanceOf(OutboundRateLimitException.class);

            assertThat(server.getRequestCount()).isEqualTo(1);
        }
    }
}
