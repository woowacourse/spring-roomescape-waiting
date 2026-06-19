package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.exception.PaymentException.PaymentRateLimitException;

class TossRateLimitRetryInterceptorTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private RestClient clientWith(int maxAttempts, long fallbackSeconds) {
        return RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .requestInterceptor(new TossRateLimitRetryInterceptor(maxAttempts, fallbackSeconds))
                .build();
    }

    @Test
    void 토스가_429_RetryAfter를_주면_대기_후_재시도해_최종_200을_받는다() {
        server.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));

        String body = clientWith(3, 1).get().uri(server.url("/").uri()).retrieve().body(String.class);

        assertThat(body).isEqualTo("ok");
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    void RetryAfter가_없으면_고정_간격으로_폴백해_재시도한다() {
        server.enqueue(new MockResponse().setResponseCode(429));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));

        String body = clientWith(3, 0).get().uri(server.url("/").uri()).retrieve().body(String.class);

        assertThat(body).isEqualTo("ok");
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    void 재시도가_maxAttempts를_넘으면_도메인_예외로_실패한다() {
        server.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        server.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));

        RestClient client = clientWith(2, 0);

        assertThatThrownBy(() -> client.get().uri(server.url("/").uri()).retrieve().body(String.class))
                .isInstanceOf(PaymentRateLimitException.class);
        assertThat(server.getRequestCount()).isEqualTo(2);
    }
}
