package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * 토스의 429 에 대한 Retry-After 백오프 재시도 검증. 실제 tossRestClient 빈을 MockWebServer 로 향하게 한다.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "toss.max-attempts=3",
        // 백오프 검증이 목적이므로 나가는 Rate Limit 은 넉넉히 둬 끼어들지 않게 한다.
        "outbound-rate-limit.capacity=100",
        "outbound-rate-limit.refill-per-second=100.0"
})
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
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("토스가 429+Retry-After 를 주면 그만큼 대기 후 재시도해 최종 200 을 받는다")
    void 토스가_429_RetryAfter면_대기후_재시도해_200() {
        // static MockWebServer 는 메서드 간 요청 수가 누적되므로 호출 전후 델타로 검증한다.
        int before = mockWebServer.getRequestCount();
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "1"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));

        String body = confirm();

        assertThat(body).contains("DONE");
        // 429 → 재시도 → 200, 총 2번 호출되었어야 한다.
        assertThat(mockWebServer.getRequestCount() - before).isEqualTo(2);
    }

    @Test
    @DisplayName("Retry-After 가 없으면 짧은 고정 간격(1초)으로 폴백해 재시도한다")
    void RetryAfter가_없으면_고정간격으로_폴백() {
        int before = mockWebServer.getRequestCount();
        mockWebServer.enqueue(new MockResponse().setResponseCode(429)); // Retry-After 헤더 없음
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));

        String body = confirm();

        assertThat(body).contains("DONE");
        assertThat(mockWebServer.getRequestCount() - before).isEqualTo(2);
    }

    @Test
    @DisplayName("재시도가 maxAttempts 를 넘어도 429 면 더 시도하지 않고 실패한다(무한 재시도 금지)")
    void maxAttempts를_넘으면_실패() {
        int before = mockWebServer.getRequestCount();
        // maxAttempts=3 → 최초 1회 + 재시도 2회 = 총 3번만 호출하고 멈춘다.
        for (int i = 0; i < 5; i++) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        }

        assertThatThrownBy(this::confirm).isInstanceOf(RestClientResponseException.class);
        assertThat(mockWebServer.getRequestCount() - before).isEqualTo(3);
    }

    private String confirm() {
        return tossRestClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class);
    }
}
