package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import roomescape.payment.ratelimit.TokenBucketRateLimiter;

class OutboundRateLimitInterceptorTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        respondWithDone();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("자체 한도를 넘기면 외부로 보내지 않고 즉시 거부한다")
    void rejectsWithoutSendingWhenOverLimit() {
        AtomicLong clock = new AtomicLong(0);
        RestClient client = clientWith(new TokenBucketRateLimiter(2, 1.0, clock::get));

        assertThat(confirm(client)).contains("DONE");
        assertThat(confirm(client)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);

        assertThatThrownBy(() -> confirm(client)).isInstanceOf(OutboundRateLimitException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("토큰이 보충되면 다시 외부로 나간다")
    void sendsAgainAfterRefill() {
        AtomicLong clock = new AtomicLong(0);
        RestClient client = clientWith(new TokenBucketRateLimiter(1, 1.0, clock::get));

        assertThat(confirm(client)).contains("DONE");
        assertThatThrownBy(() -> confirm(client)).isInstanceOf(OutboundRateLimitException.class);

        clock.addAndGet(1_000_000_000L);

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

    private void respondWithDone() {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"status\":\"DONE\"}");
            }
        });
    }
}
