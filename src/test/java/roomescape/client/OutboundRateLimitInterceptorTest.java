package roomescape.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
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

    @Test
    void 자체_한도를_넘기면_외부로_보내지_않고_즉시_거부한다() {
        var clock = new AtomicLong(0);
        var client = clientWith(new TokenBucketRateLimiter(2, 1.0, clock::get));
        enqueueDone();
        enqueueDone();

        assertThat(confirm(client)).contains("DONE");
        assertThat(confirm(client)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);

        assertThatThrownBy(() -> confirm(client)).isInstanceOf(TossOutboundRateLimitException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void 토큰이_보충되면_다시_외부로_나간다() {
        var clock = new AtomicLong(0);
        var client = clientWith(new TokenBucketRateLimiter(1, 1.0, clock::get));
        enqueueDone();

        assertThat(confirm(client)).contains("DONE");
        assertThatThrownBy(() -> confirm(client)).isInstanceOf(TossOutboundRateLimitException.class);

        clock.addAndGet(1_000_000_000L);
        enqueueDone();

        assertThat(confirm(client)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    private RestClient clientWith(TokenBucketRateLimiter rateLimiter) {
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new OutboundRateLimitInterceptor(rateLimiter))
                .build();
    }

    private String confirm(RestClient client) {
        return client.post().uri("/v1/payments/confirm").retrieve().body(String.class);
    }

    private void enqueueDone() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));
    }
}
