package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

@SpringBootTest
class RetryAfterInterceptorTest {

    static MockWebServer mockWebServer;

    static {
        mockWebServer = new MockWebServer();
        try {
            mockWebServer.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    private RestClient tossRestClient;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> mockWebServer.url("/").toString());
        registry.add("toss.max-attempts", () -> "3");
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 토스가_429와_RetryAfter를_주면_대기후_재시도해_최종_200을_받는다() {
        int countBefore = mockWebServer.getRequestCount();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));

        String body = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class);

        assertThat(body).contains("DONE");
        assertThat(mockWebServer.getRequestCount() - countBefore).isEqualTo(2);
    }

    @Test
    void maxAttempts_초과시_RateLimitExceeded_예외가_발생한다() {
        int countBefore = mockWebServer.getRequestCount();
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));

        assertThatThrownBy(() -> tossRestClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class))
                .isInstanceOf(TossPaymentException.RateLimitExceeded.class);

        assertThat(mockWebServer.getRequestCount() - countBefore).isEqualTo(3);
    }
}