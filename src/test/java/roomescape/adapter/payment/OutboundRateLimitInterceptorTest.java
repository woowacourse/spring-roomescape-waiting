package roomescape.adapter.payment;

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
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import roomescape.exception.server.OutboundRateLimitException;
import roomescape.ratelimit.TokenBucketRateLimiter;

class OutboundRateLimitInterceptorTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse().setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{}");
            }
        });
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private RestClient clientWith(TokenBucketRateLimiter limiter) {
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new OutboundRateLimitInterceptor(limiter))
                .build();
    }

    private void send(RestClient client) {
        client.post().uri("/v1/payments/confirm").retrieve().body(String.class);
    }

    @Test
    void 자체_한도를_넘기면_외부로_보내지_않고_거부한다() {
        var client = clientWith(new TokenBucketRateLimiter(2, 1.0, new AtomicLong(0)::get));

        send(client);
        send(client);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);

        assertThatThrownBy(() -> send(client)).isInstanceOf(OutboundRateLimitException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void 토큰이_보충되면_다시_외부로_나간다() {
        var clock = new AtomicLong(0);
        var client = clientWith(new TokenBucketRateLimiter(1, 1.0, clock::get));

        send(client);
        assertThatThrownBy(() -> send(client)).isInstanceOf(OutboundRateLimitException.class);

        clock.addAndGet(1_000_000_000L);
        send(client);

        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }
}
