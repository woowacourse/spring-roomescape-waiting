package roomescape.ratelimit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import roomescape.payment.OutboundRateLimitException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundRateLimitInterceptorTest {

    private final AtomicLong nanos = new AtomicLong(0);
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

    private RestClient client(long capacity, double refillPerSec) {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, refillPerSec, nanos::get);
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new OutboundRateLimitInterceptor(limiter))
                .build();
    }

    @Test
    @DisplayName("한도를 넘는 호출은 외부로 보내지 않고 거부하며, 토큰이 보충되면 다시 나간다.")
    void rejectOverLimitThenResumeAfterRefill() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));
        RestClient client = client(1, 1);

        assertThat(client.get().uri("/").retrieve().body(String.class)).isEqualTo("ok");

        assertThatThrownBy(() -> client.get().uri("/").retrieve().body(String.class))
                .isInstanceOf(OutboundRateLimitException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);

        nanos.addAndGet(1_000_000_000L);

        assertThat(client.get().uri("/").retrieve().body(String.class)).isEqualTo("ok");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }
}
