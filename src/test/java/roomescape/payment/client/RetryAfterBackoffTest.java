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
import org.springframework.web.client.HttpClientErrorException;
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

    @Test
    void RetryAfter헤더가_없으면_기본_1초_폴백으로_재시도해_최종_200을_받는다() {
        int before = mockWebServer.getRequestCount();
        // Retry-After 헤더 없이 429만 준다 → 기본 1초 폴백
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));

        var body = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class);

        assertThat(body).contains("DONE");
        // 429(헤더 없음) → 1초 대기 → 200, 총 2번 호출
        assertThat(mockWebServer.getRequestCount() - before).isEqualTo(2);
    }

    @Test
    void maxAttempts를_모두_소진하면_429_응답이_그대로_전파되어_실패한다() {
        int before = mockWebServer.getRequestCount();
        // maxAttempts=3 이므로 429를 3번 줘야 소진된다(첫 시도 + 재시도 2번).
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));

        assertThatThrownBy(() ->
                tossRestClient.post()
                        .uri("/v1/payments/confirm")
                        .retrieve()
                        .body(String.class)
        ).isInstanceOf(HttpClientErrorException.class)
                .satisfies(e -> assertThat(((HttpClientErrorException) e).getStatusCode().value()).isEqualTo(429));

        // 총 3번(maxAttempts) 호출 후 포기
        assertThat(mockWebServer.getRequestCount() - before).isEqualTo(3);
    }
}
