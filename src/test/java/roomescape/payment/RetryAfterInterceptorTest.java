package roomescape.payment;

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
        registry.add("toss.secret-key", () -> "test_gsk_dummy");
        registry.add("toss.max-attempts", () -> "3");
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 토스가_429와_RetryAfter를_주면_대기후_재시도해_최종_200을_받는다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "1"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"paymentKey\":\"pk-1\",\"orderId\":\"order-1\",\"status\":\"DONE\",\"totalAmount\":10000}"));

        String body = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class);

        assertThat(body).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void RetryAfter_없으면_1초_기본값으로_재시도한다() {
        int beforeCount = mockWebServer.getRequestCount();
        mockWebServer.enqueue(new MockResponse().setResponseCode(429));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"paymentKey\":\"pk-1\",\"orderId\":\"order-1\",\"status\":\"DONE\",\"totalAmount\":10000}"));

        String body = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class);

        assertThat(body).contains("DONE");
        assertThat(mockWebServer.getRequestCount() - beforeCount).isEqualTo(2);
    }

    @Test
    void maxAttempts_초과시_429_그대로_반환된다() {
        int beforeCount = mockWebServer.getRequestCount();
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));

        assertThatThrownBy(() -> tossRestClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class))
                .isInstanceOf(Exception.class);

        assertThat(mockWebServer.getRequestCount() - beforeCount).isEqualTo(3);
    }
}
