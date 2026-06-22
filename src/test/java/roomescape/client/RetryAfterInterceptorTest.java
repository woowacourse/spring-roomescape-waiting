package roomescape.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class RetryAfterInterceptorTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 첫_429이후_Retry_After만큼_대기하고_재시도하면_성공한다() {
        var client = clientWith(3);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "0"));
        enqueueDone();

        assertThat(confirm(client)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void Retry_After헤더가_없으면_기본_대기시간으로_재시도한다() {
        var client = clientWith(2);
        mockWebServer.enqueue(new MockResponse().setResponseCode(429));
        enqueueDone();

        assertThat(confirm(client)).contains("DONE");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void maxAttempts가_1미만이면_생성_시점에_예외가_발생한다() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new RetryAfterInterceptor(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void maxAttempts를_넘어도_429면_도메인_예외로_실패한다() {
        var client = clientWith(2);
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));

        assertThatThrownBy(() -> confirm(client)).isInstanceOf(TossRateLimitExceededException.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    private RestClient clientWith(int maxAttempts) {
        return RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .build();
    }

    private String confirm(RestClient client) {
        return client.post().uri("/v1/payments/confirm").retrieve().body(String.class);
    }

    private void enqueueDone() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"DONE\"}"));
    }
}
