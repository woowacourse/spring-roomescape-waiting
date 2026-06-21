package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class RetryAfterInterceptorTest {

    private MockWebServer mockWebServer;
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
        try {
            mockWebServer.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        restClient = RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new RetryAfterInterceptor(3))
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 토스가_429와_RetryAfter를_주면_대기후_재시도해_최종_200을_받는다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));

        String body = restClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class);

        assertThat(body).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void maxAttempts_초과시_RateLimitExceeded_예외가_발생한다() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));

        assertThatThrownBy(() -> restClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class))
                .isInstanceOf(TossPaymentException.RateLimitExceeded.class);

        assertThat(mockWebServer.getRequestCount()).isEqualTo(3);
    }
}