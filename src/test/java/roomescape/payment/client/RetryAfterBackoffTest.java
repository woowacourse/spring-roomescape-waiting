package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;

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

/**
 * 토스가 429 + Retry-After 를 주면 그만큼 대기 후 재시도해 최종 200 을 받는지 검증한다(클라이언트 백오프).
 */
@SpringBootTest
class RetryAfterBackoffTest {

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
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "1"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));

        var body = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class);

        assertThat(body).contains("DONE");
        // 429 → 재시도 → 200, 총 2번 호출되었어야 한다.
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }
}
