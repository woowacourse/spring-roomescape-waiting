package roomescape.reservation.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import roomescape.global.exception.OutboundRateLimitException;
import roomescape.global.ratelimit.TokenBucketRateLimiter;

class OutboundRateLimitInterceptorTest {

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

    @DisplayName("outbound 한도를 넘으면 Toss로 보내지 않고 즉시 거부한다.")
    @Test
    void rejects_before_external_request_when_outbound_limit_exceeded() {
        // Given: 고정된 시계에서 outbound 토큰을 2개만 가진 RestClient를 준비합니다.
        AtomicLong clock = new AtomicLong(0);
        RestClient client = clientWith(new TokenBucketRateLimiter(2, 1.0, clock::get));
        mockWebServer.enqueue(done());
        mockWebServer.enqueue(done());

        // When & Then: 세 번째 호출은 토큰이 없으므로 MockWebServer에 도달하지 않습니다.
        assertThat(confirm(client)).contains("DONE");
        assertThat(confirm(client)).contains("DONE");
        assertThatThrownBy(() -> confirm(client))
                .isInstanceOf(OutboundRateLimitException.class)
                .hasMessage("결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @DisplayName("outbound 토큰이 보충되면 다시 Toss로 요청을 보낸다.")
    @Test
    void sends_external_request_after_outbound_token_refill() {
        // Given: 초당 1개씩 토큰이 보충되는 RestClient를 준비합니다.
        AtomicLong clock = new AtomicLong(0);
        RestClient client = clientWith(new TokenBucketRateLimiter(1, 1.0, clock::get));
        mockWebServer.enqueue(done());
        mockWebServer.enqueue(done());

        // When: 첫 호출로 토큰을 소진한 뒤 1초를 진행시켜 토큰을 보충합니다.
        assertThat(confirm(client)).contains("DONE");
        assertThatThrownBy(() -> confirm(client)).isInstanceOf(OutboundRateLimitException.class);
        clock.addAndGet(1_000_000_000L);

        // Then: 보충 후 호출은 다시 MockWebServer에 도달합니다.
        assertThat(confirm(client)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @DisplayName("Retry-After 재시도 요청은 같은 승인 시도 안에서 처리되어 outbound 토큰을 추가로 소비하지 않는다.")
    @Test
    void retry_after_retry_does_not_consume_additional_outbound_token() {
        // Given: 첫 Toss 응답은 429이고 outbound 토큰은 1개뿐입니다.
        mockWebServer.enqueue(tooManyRequests());
        mockWebServer.enqueue(done());
        RestClient client = retryAfterClientWith(new TokenBucketRateLimiter(1, 1.0, () -> 0L));

        // When & Then: 최초 승인 시도만 outbound limit을 통과하고 Retry-After 재시도는 같은 시도 안에서 처리됩니다.
        assertThat(confirm(client)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    private RestClient clientWith(TokenBucketRateLimiter rateLimiter) {
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new OutboundRateLimitInterceptor(rateLimiter))
                .build();
    }

    private RestClient retryAfterClientWith(TokenBucketRateLimiter rateLimiter) {
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new OutboundRateLimitInterceptor(rateLimiter))
                .requestInterceptor(new RetryAfterInterceptor(2))
                .build();
    }

    private String confirm(RestClient client) {
        return client.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class);
    }

    private MockResponse done() {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}");
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
}
