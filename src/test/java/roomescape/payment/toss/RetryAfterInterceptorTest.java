package roomescape.payment.toss;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class RetryAfterInterceptorTest {

    MockWebServer mockWebServer;
    RestClient restClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
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
    void 게이트웨이가_429와_RetryAfter를_주면_대기_후_재시도해_최종_200을_받는다() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader(HttpHeaders.RETRY_AFTER, "1"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("OK"));

        String body = restClient.get().uri("/test")
                .retrieve()
                .body(String.class);

        assertThat(body).isEqualTo("OK");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void RetryAfter_헤더가_없으면_기본_1초_대기_후_재시도한다() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429)); // 헤더 없음
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));

        String body = restClient.get().uri("/test")
                .retrieve()
                .body(String.class);

        assertThat(body).isEqualTo("OK");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void maxAttempts를_초과하면_마지막_429를_그대로_반환한다() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader(HttpHeaders.RETRY_AFTER, "1"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader(HttpHeaders.RETRY_AFTER, "1"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader(HttpHeaders.RETRY_AFTER, "1"));

        // maxAttempts=3 → 3번 시도 후 429를 그대로 반환
        // retrieve()는 4xx를 예외로 올리므로, onStatus로 억제해서 상태코드 직접 확인
        var response = restClient.get().uri("/test")
                .retrieve()
                .onStatus(status -> true, (req, res) -> {}) // 예외 억제
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(3);
    }
}
