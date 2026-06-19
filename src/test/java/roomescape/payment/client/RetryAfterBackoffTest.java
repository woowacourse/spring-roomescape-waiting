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
        registry.add("toss.retry.max-attempts", () -> "3");
        registry.add("outbound-rate-limit.capacity", () -> "100"); // 테스트 중 이그레스 한도 방해 방지
        registry.add("outbound-rate-limit.refill-per-second", () -> "100");
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 게이트웨이가_429와_RetryAfter를_주면_대기후_재시도해_최종_200을_받는다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "0")); // 0초 대기 → 테스트 빠르게
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));

        var body = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class);

        assertThat(body).contains("DONE");
        // 429 → 재시도 → 200, 총 2번 호출
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }
}
