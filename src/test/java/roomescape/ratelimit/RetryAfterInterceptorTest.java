package roomescape.ratelimit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import roomescape.payment.TossRateLimitException;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryAfterInterceptorTest {

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

    private RestClient client(int maxAttempts, long fallbackSeconds) {
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts, fallbackSeconds))
                .build();
    }

    @Test
    @DisplayName("429+Retry-After를 받으면 그만큼 대기 후 재시도해 200을 받는다.")
    void retryRespectingRetryAfter() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));

        long start = System.nanoTime();
        String body = client(3, 1).get().uri("/").retrieve().body(String.class);
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        assertThat(body).isEqualTo("ok");
        assertThat(elapsed).isGreaterThanOrEqualTo(Duration.ofMillis(900));
    }

    @Test
    @DisplayName("Retry-After가 없으면 고정 간격으로 폴백해 재시도한다.")
    void retryWithFallbackInterval() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));

        long start = System.nanoTime();
        String body = client(3, 1).get().uri("/").retrieve().body(String.class);
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        assertThat(body).isEqualTo("ok");
        assertThat(elapsed).isGreaterThanOrEqualTo(Duration.ofMillis(900));
    }

    @Test
    @DisplayName("maxAttempts를 넘어도 429면 도메인 예외로 실패한다.")
    void failAfterMaxAttempts() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));

        assertThatThrownBy(() -> client(2, 1).get().uri("/").retrieve().body(String.class))
                .isInstanceOf(TossRateLimitException.class);
    }
}
