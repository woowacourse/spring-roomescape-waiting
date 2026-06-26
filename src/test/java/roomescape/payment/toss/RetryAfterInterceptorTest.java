package roomescape.payment.toss;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryAfterInterceptorTest {

    private static final String SUCCESS_BODY = "{\"result\":\"ok\"}";

    private MockWebServer mockWebServer;
    private List<Duration> recordedWaits;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        recordedWaits = new ArrayList<>();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private RestClient clientWith(int maxAttempts, Duration defaultBackoff) {
        Sleeper recordingSleeper = recordedWaits::add;
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestFactory(new SimpleClientHttpRequestFactory())
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts, defaultBackoff, recordingSleeper))
                .build();
    }

    @Test
    void 토스가_429와_RetryAfter를_주면_그만큼_대기_후_재시도해_200을_받는다() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "2"));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY));

        String body = clientWith(3, Duration.ofSeconds(1)).get().retrieve().body(String.class);

        assertThat(body).contains("ok");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
        assertThat(recordedWaits).containsExactly(Duration.ofSeconds(2));
    }

    @Test
    void RetryAfter가_없으면_기본_간격으로_폴백_재시도한다() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429));
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(SUCCESS_BODY));

        clientWith(3, Duration.ofSeconds(1)).get().retrieve().body(String.class);

        assertThat(recordedWaits).containsExactly(Duration.ofSeconds(1));
    }

    @Test
    void 재시도가_maxAttempts를_넘어도_429면_도메인_예외를_던진다() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));

        assertThatThrownBy(() -> clientWith(2, Duration.ofSeconds(1)).get().retrieve().body(String.class))
                .isInstanceOf(TossRateLimitException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }
}
