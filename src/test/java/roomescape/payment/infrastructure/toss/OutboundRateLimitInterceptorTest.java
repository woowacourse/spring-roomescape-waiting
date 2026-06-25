package roomescape.payment.infrastructure.toss;

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
import roomescape.common.ratelimit.TokenBucketRateLimiter;
import roomescape.payment.domain.exception.OutboundRateLimitException;

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
    void 자체_한도를_넘기면_외부로_보내지_않고_즉시_거부한다() {
        AtomicLong clock = new AtomicLong(0);
        RestClient client = clientWith(new TokenBucketRateLimiter(2, 1.0, clock::get));

        assertThat(confirm(client)).contains("DONE");
        assertThat(confirm(client)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);

        assertThatThrownBy(() -> confirm(client)).isInstanceOf(OutboundRateLimitException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void 토큰이_보충되면_다시_외부로_나간다() {
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
